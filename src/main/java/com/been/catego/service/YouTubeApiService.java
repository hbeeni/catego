package com.been.catego.service;

import com.been.catego.dto.response.PageTokenResponse;
import com.been.catego.dto.response.SubscriptionResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.util.YouTubeApiUtil;
import com.been.catego.util.YoutubeConvertUtils;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.been.catego.service.YouTubePart.PLAYER;
import static com.been.catego.service.YouTubePart.SNIPPET;
import static com.been.catego.service.YouTubePart.STATISTICS;
import static com.been.catego.service.YouTubePart.convertToPartStrings;
import static com.been.catego.util.YoutubeConvertUtils.convertToVideoIds;

@RequiredArgsConstructor
@Service
public class YouTubeApiService {

    private static final YouTube youTube;
    private final YouTubeApiUtil youtubeApiUtil;

    static {
        youTube = YouTubeApiUtil.youTube();
    }

    public List<Channel> findChannels(SubscriptionListResponse subscriptionListResponse) {
        List<String> subscriptionIds = YoutubeConvertUtils.convertToSubscriptionIds(subscriptionListResponse);
        return getChannelsByIds(subscriptionIds);
    }

    public List<SubscriptionResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = new ArrayList<>();
        String nextPageToken = null;

        do {
            SubscriptionListResponse subscriptionListResponse = getSubscriptionListResponse(nextPageToken, 50L);
            subscriptions.addAll(subscriptionListResponse.getItems());
            nextPageToken = subscriptionListResponse.getNextPageToken();
        } while (nextPageToken != null);

        return subscriptions.stream()
                .map(SubscriptionResponse::from)
                .collect(Collectors.toList());
    }

    public WithPageTokenResponse<List<SubscriptionResponse>> getSubscriptionsWithPageToken(String pageToken,
                                                                                           long maxResult) {
        SubscriptionListResponse subscriptionListResponse = getSubscriptionListResponse(pageToken, maxResult);

        PageTokenResponse pageTokenResponse = new PageTokenResponse(subscriptionListResponse.getPrevPageToken(),
                subscriptionListResponse.getNextPageToken());
        List<SubscriptionResponse> data = subscriptionListResponse.getItems().stream()
                .map(SubscriptionResponse::from)
                .toList();

        return new WithPageTokenResponse<>(data, pageTokenResponse);
    }

    /**
     * part 기본 값: snippet
     */
    public SubscriptionListResponse getSubscriptionListResponse(String pageToken, long maxResult) {
        return getSubscriptionListResponse(pageToken, maxResult, SNIPPET);
    }

    /**
     * 알파벳 순으로 정렬된 결과를 반환합니다.
     *
     * @param parts contentDetails, id, snippet, subscriberSnippet
     */
    public SubscriptionListResponse getSubscriptionListResponse(String pageToken, long maxResult,
                                                                YouTubePart... parts) {
        try {
            YouTube.Subscriptions.List subscriptionsList = youTube.subscriptions().list(convertToPartStrings(parts));

            youtubeApiUtil.setYouTubeRequest(subscriptionsList);
            subscriptionsList.setMine(true);
            subscriptionsList.setOrder("alphabetical");
            subscriptionsList.setMaxResults(maxResult);
            subscriptionsList.setPageToken(pageToken);

            return subscriptionsList.execute();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    /**
     * part 기본 값: snippet, statistics
     */
    public List<Channel> getChannelsByIds(List<String> channelIds) {
        return getChannelsByIds(channelIds, SNIPPET, STATISTICS);
    }

    /**
     * @param parts auditDetails, brandingSettings, contentDetails, contentOwnerDetails, id, localizations, snippet,
     *              statistics, status, topicDetails
     */
    public List<Channel> getChannelsByIds(List<String> channelIds, YouTubePart... parts) {
        try {
            YouTube.Channels.List channelsList = youTube.channels().list(convertToPartStrings(parts));

            youtubeApiUtil.setYouTubeRequest(channelsList);
            channelsList.setId(channelIds);

            return channelsList.execute().getItems();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    /**
     * part 기본 값: snippet, statistics, player
     */
    public VideoListResponse getVideoListResponseByChannelId(String channelId, long maxResult) {
        return getVideoListResponseByChannelId(channelId, maxResult, SNIPPET, STATISTICS, PLAYER);
    }

    /**
     * @param parts contentDetails, fileDetails, id, liveStreamingDetails, localizations, player, processingDetails,
     *              recordingDetails, snippet, statistics, status, suggestions, topicDetails
     */
    public VideoListResponse getVideoListResponseByChannelId(String channelId, long maxResult, YouTubePart... parts) {
        try {
            SearchListResponse searchListResponse = searchVideosByChannelId(channelId, maxResult);
            List<String> videoIds = convertToVideoIds(searchListResponse);

            YouTube.Videos.List videoList = youTube.videos().list(convertToPartStrings(parts));

            youtubeApiUtil.setYouTubeRequest(videoList);
            videoList.setId(videoIds);

            return videoList.execute();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    /**
     * date 기준으로 정렬한 결과를 반환합니다.
     */
    private SearchListResponse searchVideosByChannelId(String channelId, long maxResult) {
        try {
            YouTube.Search.List searchList = youTube.search().list(convertToPartStrings(SNIPPET));

            youtubeApiUtil.setYouTubeRequest(searchList);
            searchList.setChannelId(channelId);
            searchList.setOrder("date");
            searchList.setType(List.of("video"));
            searchList.setMaxResults(maxResult);

            return searchList.execute();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }
}
