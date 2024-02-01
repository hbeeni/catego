package com.been.catego.service;

import com.been.catego.domain.Channel;
import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.ChannelDto;
import com.been.catego.dto.response.FolderInfoWithChannelResponse;
import com.been.catego.dto.response.FolderResponse;
import com.been.catego.dto.response.SubscriptionResponse;
import com.been.catego.dto.response.VideoResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.repository.ChannelRepository;
import com.been.catego.repository.FolderChannelRepository;
import com.been.catego.repository.FolderRepository;
import com.been.catego.repository.UserRepository;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.been.catego.util.YoutubeConvertUtils.convertToLocalDateTime;

@RequiredArgsConstructor
@Transactional
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final ChannelRepository channelRepository;
    private final FolderChannelRepository folderChannelRepository;
    private final UserRepository userRepository;

    private final YouTubeApiService youTubeApiService;

    @Transactional(readOnly = true)
    public List<FolderInfoWithChannelResponse> getAllFolderInfoWithChannelsByUserId(Long userId) {
        List<Folder> folders = folderRepository.findAllByUser_IdOrderByNameAsc(userId);
        setFolderChannels(folders);

        return folders.stream()
                .map(FolderInfoWithChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FolderResponse getFolderInfo(Long folderId, Long userId) {
        Folder folder = folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));
        return FolderResponse.from(folder);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptionsWithInclusionStatusInFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));

        Set<String> channelIdsInFolder = folder.getFolderChannels().stream()
                .map(FolderChannel::getChannel)
                .map(Channel::getId)
                .collect(Collectors.toSet());

        List<SubscriptionResponse> subscriptions = youTubeApiService.getAllSubscriptions();
        subscriptions.stream()
                .filter(subscription -> channelIdsInFolder.contains(subscription.getChannelId()))
                .forEach(SubscriptionResponse::setIncludedInFolderTrue);
        subscriptions.sort(Comparator.comparing(SubscriptionResponse::isIncludedInFolder).reversed()
                .thenComparing(subscription -> subscription.getChannelTitle().toLowerCase()));

        return subscriptions;
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> getFolderVideos(Long userId, Long folderId) {
        List<String> channelIds = getChannelIdsForFolder(userId, folderId);

        HashMap<String, com.google.api.services.youtube.model.Channel> youTubeChannelIdToYouTubeChannelMap =
                new HashMap<>();
        youTubeApiService.getChannelsByIds(channelIds)
                .forEach(channel -> youTubeChannelIdToYouTubeChannelMap.put(channel.getId(), channel));

        //각 채널별로 비디오 50개씩 가져오기
        List<VideoListResponse> videoListResponses = channelIds.stream()
                .map(channelId -> youTubeApiService.getVideoListResponseByChannelId(channelId, 50L))
                .toList();

        List<Video> videos = new ArrayList<>();
        videoListResponses.forEach(videoListResponse -> videos.addAll(videoListResponse.getItems()));
        videos.sort(new VideoPublishedAtComparator());

        return videos.stream()
                .map(video -> VideoResponse.from(video,
                        youTubeChannelIdToYouTubeChannelMap.get(video.getSnippet().getChannelId())))
                .toList();
    }

    public Long createFolder(Long userId, String folderName, Map<String, ChannelDto> channelIdToChannelDtoMap) {
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

        return folderRepository.save(folder).getId();
    }

    public void editFolder(Long folderId, Long userId, String folderName,
                           Map<String, ChannelDto> channelIdToChannelDtoMap) {
        //해당 폴더 가져오기
        Folder folder = folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));

        //폴더 이름 변경
        folder.updateName(folderName);

        //새로운 채널 저장
        List<Channel> channels = saveNewChannels(channelIdToChannelDtoMap);

        //기존 채널은 모두 삭제
        List<FolderChannel> oldFolderChannels = folder.getFolderChannels();
        folderChannelRepository.deleteAllInBatch(oldFolderChannels);

        List<FolderChannel> newFolderChannels = channels.stream()
                .map(channel -> FolderChannel.builder()
                        .folder(folder)
                        .channel(channel)
                        .build())
                .toList();
        folder.setFolderChannels(newFolderChannels);
    }

    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));
        List<FolderChannel> folderChannels = folder.getFolderChannels();

        folderChannelRepository.deleteAllInBatch(folderChannels);
        folderRepository.delete(folder);
    }

    /**
     * DB에 저장되어 있지 않은 채널을 저장한 후, 원래 저장되어 있던 채널과 저장한 채널 리스트를 반환한다.
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

    private void setFolderChannels(List<Folder> folders) {
        Map<Long, List<FolderChannel>> folderChannelMap = findFolderIdToFolderChannelMap(toFolderIds(folders));
        folders.forEach(f -> f.setFolderChannels(folderChannelMap.get(f.getId())));
    }

    private List<Long> toFolderIds(List<Folder> folders) {
        return folders.stream().map(Folder::getId).toList();
    }

    private Map<Long, List<FolderChannel>> findFolderIdToFolderChannelMap(List<Long> folderIds) {
        List<FolderChannel> folderChannels = folderChannelRepository.findAllByFolderIdIn(folderIds);
        return folderChannels.stream()
                .collect(Collectors.groupingBy(folderChannel -> folderChannel.getFolder().getId()));
    }

    private List<String> getChannelIdsForFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findFolderByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_FOLDER));

        return folder.getFolderChannels().stream()
                .map(folderChannel -> folderChannel.getChannel().getId())
                .toList();
    }

    private static class VideoPublishedAtComparator implements Comparator<Video> {

        @Override
        public int compare(Video v1, Video v2) {
            LocalDateTime v2LDT = convertToLocalDateTime(v2.getSnippet().getPublishedAt());
            LocalDateTime v1LDT = convertToLocalDateTime(v1.getSnippet().getPublishedAt());

            return v2LDT.compareTo(v1LDT);
        }

    }
}
