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
import com.been.catego.repository.FolderRepository;
import com.been.catego.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;
    private final FolderChannelRepository folderChannelRepository;
    private final UserRepository userRepository;

    private final YouTubeApiDataService youTubeApiDataService;

    @Transactional(readOnly = true)
    public List<FolderInfoWithChannelInfoResponse> getAllFoldersWithChannelsByUserId(Long userId) {
        List<Folder> folders = folderRepository.findAllByUser_IdOrderByNameAsc(userId);

        List<Long> folderIds = folders.stream().map(Folder::getId).toList();

        List<FolderChannel> folderChannels = folderChannelRepository.findAllByFolderIds(folderIds);

        Map<Long, List<FolderChannel>> folderChannelMap = folderChannels.stream()
                .collect(Collectors.groupingBy(folderChannel -> folderChannel.getFolder().getId()));

        folders.forEach(folder -> folder.setFolderChannels(folderChannelMap.get(folder.getId())));

        return folders.stream()
                .map(FolderInfoWithChannelInfoResponse::from)
                .toList();
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
        List<Channel> channels = saveNewChannels(channelIdToChannelDtoMap);

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
    }

    public void editFolder(Long folderId, Long userId, String folderName,
                           Map<String, ChannelDto> channelIdToChannelDtoMap) {

        //해당 폴더 가져오기
        Folder folder = getFolderOrException(folderId, userId);

        //폴더 이름 변경
        folder.updateName(folderName);

        //새로운 채널 저장
        List<Channel> channels = saveNewChannels(channelIdToChannelDtoMap);

        //폴더의 기존 채널은 모두 삭제
        List<FolderChannel> oldFolderChannels = folder.getFolderChannels();
        folderChannelRepository.deleteAllInBatch(oldFolderChannels);

        //폴더에 채널 새로 저장
        List<FolderChannel> newFolderChannels = channels.stream()
                .map(channel -> FolderChannel.builder()
                        .folder(folder)
                        .channel(channel)
                        .build())
                .toList();
        folder.setFolderChannels(newFolderChannels);
    }

    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = getFolderOrException(folderId, userId);
        List<FolderChannel> folderChannels = folder.getFolderChannels();

        folderChannelRepository.deleteAllInBatch(folderChannels);
        folderRepository.delete(folder);
    }

    private Folder getFolderOrException(Long folderId, Long userId) {
        return folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));
    }

    /**
     * DB에 저장되어 있지 않은 채널을 저장한 후, 원래 저장되어 있던 채널과 저장한 채널을 리스트로 반환한다.
     */
    private List<Channel> saveNewChannels(Map<String, ChannelDto> channelIdToChannelDtoMap) {
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
