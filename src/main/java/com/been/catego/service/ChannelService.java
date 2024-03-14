package com.been.catego.service;

import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.response.ChannelResponse;
import com.been.catego.dto.response.ChannelWithFolderNamesResponse;
import com.been.catego.dto.response.DataWithPageTokenResponse;
import com.been.catego.dto.response.PageTokenResponse;
import com.been.catego.dto.response.VideoResponse;
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

    private final YouTubeApiDataService youTubeApiDataService;

    /**
     * 유튜브 구독 채널을 폴더 이름과 함께 반환한다.
     */
    public DataWithPageTokenResponse<List<ChannelWithFolderNamesResponse>> findSubscriptionChannelsWithFolderNames(
            Long userId, String pageToken, long maxResult) {

        SubscriptionListResponse subscriptionListResponse =
                youTubeApiDataService.getSubscriptionListResponse(pageToken, maxResult);

        List<Channel> youTubeChannels = youTubeApiDataService.getChannels(subscriptionListResponse);

        List<ChannelWithFolderNamesResponse> data =
                getChannelWithFolderNamesSortedByChannelTitle(userId, youTubeChannels);
        PageTokenResponse pageTokenResponse = new PageTokenResponse(subscriptionListResponse.getPrevPageToken(),
                subscriptionListResponse.getNextPageToken());

        return new DataWithPageTokenResponse<>(data, pageTokenResponse);
    }

    /**
     * 채널의 정보를 반환한다.
     */
    public ChannelResponse getChannelInfo(String channelId) {
        return ChannelResponse.from(youTubeApiDataService.getChannelById(channelId));
    }

    /**
     * 채널의 동영상을 페이지 토큰과 함께 반환한다.
     *
     * @param channelId 채널 ID
     * @param maxResult 동영상 개수, 최대 50
     */
    public DataWithPageTokenResponse<List<VideoResponse>> getVideosOfChannel(
            String channelId, int maxResult, String pageToken) {

        PlaylistItemListResponse playlistItemListResponse =
                youTubeApiDataService.getVideosByChannelId(channelId, maxResult, pageToken);

        PageTokenResponse pageTokenResponse = new PageTokenResponse(playlistItemListResponse.getPrevPageToken(),
                playlistItemListResponse.getNextPageToken());

        List<String> videoIds = convertToVideoIds(playlistItemListResponse);

        List<VideoResponse> data = youTubeApiDataService.getVideosByIds(videoIds).stream()
                .map(VideoResponse::from)
                .toList();

        return new DataWithPageTokenResponse<>(data, pageTokenResponse);
    }

    private List<ChannelWithFolderNamesResponse> getChannelWithFolderNamesSortedByChannelTitle(Long userId,
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
        List<FolderChannel> folderChannels = folderChannelRepository.findAllByFolderIds(folderIds);
        return folderChannels.stream()
                .collect(Collectors.groupingBy(folderChannel -> folderChannel.getChannel().getId()));
    }

    private List<Long> toFolderIds(List<Folder> folders) {
        return folders.stream()
                .map(Folder::getId)
                .toList();
    }
}
