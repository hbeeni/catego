package com.been.catego.service;

import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.response.ChannelResponse;
import com.been.catego.dto.response.ChannelWithFolderNamesResponse;
import com.been.catego.dto.response.PageTokenResponse;
import com.been.catego.dto.response.VideoResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.repository.FolderChannelRepository;
import com.been.catego.repository.FolderRepository;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.been.catego.util.YoutubeConvertUtils.convertToVideoIds;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChannelService {

    private final FolderRepository folderRepository;
    private final FolderChannelRepository folderChannelRepository;

    private final YouTubeApiService youTubeApiService;

    public WithPageTokenResponse<List<ChannelWithFolderNamesResponse>> findSubscriptionChannelsWithFolderNames(
            Long userId, String pageToken, long maxResult) {
        //구독 채널 가져오기
        SubscriptionListResponse subscriptionListResponse =
                youTubeApiService.getSubscriptionListResponse(pageToken, maxResult);
        List<Channel> youTubeChannels = youTubeApiService.findChannels(subscriptionListResponse);

        List<ChannelWithFolderNamesResponse> data = getChannelResponsesSortedByChannelTitle(userId, youTubeChannels);
        PageTokenResponse pageTokenResponse = new PageTokenResponse(subscriptionListResponse.getPrevPageToken(),
                subscriptionListResponse.getNextPageToken());

        return new WithPageTokenResponse<>(data, pageTokenResponse);
    }

    public ChannelResponse getChannelInfo(String channelId) {
        return youTubeApiService.findChannel(channelId)
                .map(ChannelResponse::from)
                .orElseThrow(() -> new CustomException(ErrorMessages.NOT_FOUND_CHANNEL));
    }

    public WithPageTokenResponse<List<VideoResponse>> getVideosForChannel(String channelId, int maxResult,
                                                                          String pageToken) {
        PlaylistItemListResponse playlistItemListResponse =
                youTubeApiService.getVideosByChannelId(channelId, maxResult, pageToken);

        PageTokenResponse pageTokenResponse = new PageTokenResponse(playlistItemListResponse.getPrevPageToken(),
                playlistItemListResponse.getNextPageToken());

        List<VideoResponse> data = youTubeApiService.getVideosByIds(convertToVideoIds(playlistItemListResponse))
                .stream()
                .map(VideoResponse::from)
                .toList();
        return new WithPageTokenResponse<>(data, pageTokenResponse);
    }

    private List<ChannelWithFolderNamesResponse> getChannelResponsesSortedByChannelTitle(Long userId,
                                                                                         List<Channel> youTubeChannels) {
        List<Folder> folders = folderRepository.findAllByUser_IdOrderByNameAsc(userId);
        Map<String, List<FolderChannel>> channelIdToFolderChannelsMap =
                getChannelIdToFolderChannelsMap(toFolderIds(folders));

        return youTubeChannels.stream()
                .map(youTubeChannel -> ChannelWithFolderNamesResponse.from(youTubeChannel,
                        getFoldersForChannel(channelIdToFolderChannelsMap, youTubeChannel)))
                .sorted(Comparator.comparing(
                        channelWithFolderNamesResponse -> channelWithFolderNamesResponse.channelTitle().toLowerCase()))
                .toList();
    }

    private static List<Folder> getFoldersForChannel(Map<String, List<FolderChannel>> channelToFolderChannelsMap,
                                                     Channel youTubeChannel) {
        if (channelToFolderChannelsMap.get(youTubeChannel.getId()) == null) {
            return List.of();
        }

        return channelToFolderChannelsMap.get(youTubeChannel.getId()).stream()
                .map(FolderChannel::getFolder)
                .toList();
    }

    private Map<String, List<FolderChannel>> getChannelIdToFolderChannelsMap(List<Long> folderIds) {
        List<FolderChannel> folderChannels = folderChannelRepository.findAllByFolderIdIn(folderIds);
        return folderChannels.stream()
                .collect(Collectors.groupingBy(folderChannel -> folderChannel.getChannel().getId()));
    }

    private List<Long> toFolderIds(List<Folder> folders) {
        return folders.stream()
                .map(Folder::getId)
                .toList();
    }
}
