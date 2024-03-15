package com.been.catego.service;

import com.been.catego.domain.Channel;
import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.ChannelDto;
import com.been.catego.dto.response.FolderInfoWithChannelInfoResponse;
import com.been.catego.dto.response.FolderResponse;
import com.been.catego.dto.response.SubscriptionChannelResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.repository.ChannelRepository;
import com.been.catego.repository.FolderChannelRepository;
import com.been.catego.repository.FolderRedisRepository;
import com.been.catego.repository.FolderRepository;
import com.been.catego.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;
    private final FolderChannelRepository folderChannelRepository;
    private final UserRepository userRepository;
    private final FolderRedisRepository folderRedisRepository;

    private final YouTubeApiDataService youTubeApiDataService;

    @Transactional(readOnly = true)
    public List<FolderInfoWithChannelInfoResponse> getAllFoldersWithChannelsByUserId(Long userId) {
        List<FolderInfoWithChannelInfoResponse> foldersFromRedis = folderRedisRepository.getFolders(userId);

        if (!foldersFromRedis.isEmpty()) {
            log.info("[getAllFoldersWithChannelsByUserId] REDIS getFolders success");
            return foldersFromRedis;
        }

        List<Folder> folders = folderRepository.findAllByUser_IdOrderByNameAsc(userId);

        List<Long> folderIds = folders.stream().map(Folder::getId).toList();

        List<FolderChannel> folderChannels = folderChannelRepository.findAllByFolderIds(folderIds);

        Map<Long, List<FolderChannel>> folderChannelMap = folderChannels.stream()
                .collect(Collectors.groupingBy(folderChannel -> folderChannel.getFolder().getId()));

        folders.forEach(folder -> folder.setFolderChannels(folderChannelMap.get(folder.getId())));

        List<FolderInfoWithChannelInfoResponse> foldersFromDB = folders.stream()
                .map(FolderInfoWithChannelInfoResponse::from)
                .toList();

        //redis save
        folderRedisRepository.saveFolders(userId, foldersFromDB);

        return foldersFromDB;
    }

    @Transactional(readOnly = true)
    public FolderResponse getFolderInfo(Long folderId, Long userId) {
        Folder folder = getFolderOrException(folderId, userId);
        return FolderResponse.from(folder);
    }

    /**
     * 구독한 모든 유튜브 채널을 반환한다. 폴더에 속한 채널순 -> 채널 이름순으로 정렬된다.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionChannelResponse> getAllSubscriptionsInFolder(Long folderId, Long userId) {

        Folder folder = getFolderOrException(folderId, userId);

        Set<String> channelIdsInFolder = folder.getFolderChannels().stream()
                .map(FolderChannel::getChannel)
                .map(Channel::getId)
                .collect(Collectors.toSet());

        List<SubscriptionChannelResponse> subscriptionChannels = youTubeApiDataService.getAllSubscriptionChannels()
                .stream()
                .map(SubscriptionChannelResponse::from)
                .collect(Collectors.toList());

        subscriptionChannels.stream()
                .filter(subscription -> channelIdsInFolder.contains(subscription.getChannelId()))
                .forEach(SubscriptionChannelResponse::setIncludedInFolderTrue);

        subscriptionChannels.sort(Comparator.comparing(SubscriptionChannelResponse::isIncludedInFolder).reversed()
                .thenComparing(subscription -> subscription.getChannelTitle().toLowerCase()));

        return subscriptionChannels;
    }

    public void createFolder(Long userId, String folderName, Map<String, ChannelDto> channelIdToChannelDtoMap) {
        List<Channel> channels = saveNewChannelsAndReturnAllChannels(channelIdToChannelDtoMap);

        //folder 생성
        Folder folder = Folder.builder()
                .user(userRepository.getReferenceById(userId))
                .name(folderName)
                .build();

        //FolderChannel 생성
        List<FolderChannel> folderChannels = channels.stream()
                .map(channel -> FolderChannel.builder()
                        .folder(folder)
                        .channel(channel)
                        .build())
                .toList();

        folder.setFolderChannels(folderChannels);
        folderRepository.save(folder);

        folderRedisRepository.deleteFolders(userId);
    }

    public void editFolder(Long folderId, Long userId, String folderName,
                           Map<String, ChannelDto> channelIdToChannelDtoMap) {

        //해당 폴더 가져오기
        Folder folder = getFolderOrException(folderId, userId);

        //폴더 이름 변경
        if (StringUtils.hasText(folderName)) {
            folder.updateName(folderName);
        }

        List<Channel> selectedChannels = saveNewChannelsAndReturnAllChannels(channelIdToChannelDtoMap);
        List<FolderChannel> folderChannels = folder.getFolderChannels();

        Map<String, FolderChannel> existingChannelIdMap = folderChannels.stream()
                .collect(toMap(
                        folderChannel -> folderChannel.getChannel().getId(),
                        Function.identity()
                ));

        removeExcludedChannelsFromFolder(folderChannels, selectedChannels, existingChannelIdMap);
        saveAddedChannelsToFolder(folder, folderChannels, selectedChannels, existingChannelIdMap);

        folderRedisRepository.deleteFolders(userId);
    }

    /**
     * 폴더에 새롭게 추가된 채널 저장
     */
    private static void saveAddedChannelsToFolder(Folder folder, List<FolderChannel> folderChannels,
                                                  List<Channel> selectedChannels,
                                                  Map<String, FolderChannel> existingChannelIdMap) {

        for (Channel channel : selectedChannels) {
            if (!existingChannelIdMap.containsKey(channel.getId())) {
                folderChannels.add(FolderChannel.builder()
                        .folder(folder)
                        .channel(channel)
                        .build());
            }
        }
    }

    /**
     * 폴더의 기존 채널 중 제외된 채널 삭제
     */
    private void removeExcludedChannelsFromFolder(List<FolderChannel> folderChannels, List<Channel> selectedChannels,
                                                  Map<String, FolderChannel> existingChannelIdMap) {

        Set<String> channelIdSet = selectedChannels.stream().map(Channel::getId).collect(toSet());

        Set<FolderChannel> excludedChannel = existingChannelIdMap.entrySet().stream()
                .filter(entry -> !channelIdSet.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(toSet());

        folderChannels.removeAll(excludedChannel);
        folderChannelRepository.deleteAllInBatch(excludedChannel);
    }

    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = getFolderOrException(folderId, userId);
        List<FolderChannel> folderChannels = folder.getFolderChannels();

        folderChannelRepository.deleteAllInBatch(folderChannels);
        folderRepository.delete(folder);

        folderRedisRepository.deleteFolders(userId);
    }

    private Folder getFolderOrException(Long folderId, Long userId) {
        return folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));
    }

    /**
     * DB에 저장되어 있지 않은 채널을 저장한 후, 원래 저장되어 있던 채널과 저장한 채널을 리스트로 반환한다.
     */
    private List<Channel> saveNewChannelsAndReturnAllChannels(Map<String, ChannelDto> channelIdToChannelDtoMap) {
        List<Channel> channels = channelRepository.findAllById(channelIdToChannelDtoMap.keySet());
        Set<String> existingChannelIds = channels.stream()
                .map(Channel::getId)
                .collect(Collectors.toSet());

        //저장되지 않은 채널은 저장하기
        channelIdToChannelDtoMap.values().stream()
                .filter(channelDto -> !existingChannelIds.contains(channelDto.id()))
                .forEach(channelDto -> {
                    Channel savedChannel = channelRepository.save(channelDto.toEntity());
                    channels.add(savedChannel);
                });

        return channels;
    }
}
