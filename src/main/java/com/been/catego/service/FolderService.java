package com.been.catego.service;

import com.been.catego.domain.Channel;
import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.ChannelDto;
import com.been.catego.dto.response.VideoResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.repository.ChannelRepository;
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
    private final UserRepository userRepository;

    private final YouTubeApiService youTubeApiService;

    public Long createFolder(Long userId, String listName, Map<String, ChannelDto> channelIdToChannelDtoMap) {
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

        //folder 생성
        Folder folder = Folder.builder()
                .user(userRepository.getReferenceById(userId))
                .name(listName)
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
