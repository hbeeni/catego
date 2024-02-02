package com.been.catego.service;

import com.been.catego.dto.response.CommentWithRepliesResponse;
import com.been.catego.dto.response.PageTokenResponse;
import com.been.catego.dto.response.SubscriptionResponse;
import com.been.catego.dto.response.VideoPlayerDetailResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.util.YouTubeApiUtil;
import com.been.catego.util.YoutubeConvertUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.been.catego.service.YouTubePart.ID;
import static com.been.catego.service.YouTubePart.REPLIES;
import static com.been.catego.service.YouTubePart.SNIPPET;
import static com.been.catego.service.YouTubePart.STATISTICS;
import static com.been.catego.service.YouTubePart.convertToPartStrings;
import static com.been.catego.util.YoutubeConvertUtils.convertToUploadPlaylistId;
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
        List<String> subscriptionChannelIds = YoutubeConvertUtils.convertToSubscriptionIds(subscriptionListResponse);
        return getChannelsByIds(subscriptionChannelIds);
    }

    public Optional<Channel> findChannel(String channelId) {
        return Optional.ofNullable(getChannelById(channelId));
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
     * 알파벳 순으로 정렬된 결과를 반환합니다.
     */
    public SubscriptionListResponse getSubscriptionListResponse(String pageToken, long maxResult) {
        try {
            YouTube.Subscriptions.List subscriptionsList = youTube.subscriptions().list(convertToPartStrings(SNIPPET));

            youtubeApiUtil.setYouTubeRequest(subscriptionsList);
            subscriptionsList.setMine(true);
            subscriptionsList.setOrder("alphabetical");
            subscriptionsList.setMaxResults(maxResult);
            subscriptionsList.setPageToken(pageToken);

            return subscriptionsList.execute();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    private Channel getChannelById(String channelId) {
        return getChannelsByIds(List.of(channelId)).get(0);
    }

    public List<Channel> getChannelsByIds(List<String> channelIds) {
        try {
            YouTube.Channels.List channelsList = youTube.channels().list(convertToPartStrings(SNIPPET, STATISTICS));

            youtubeApiUtil.setYouTubeRequest(channelsList);
            channelsList.setId(channelIds);

            return channelsList.execute().getItems();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    public List<Video> getVideosByIds(List<String> videoIds) {
        try {
            YouTube.Videos.List videoList = youTube.videos().list(convertToPartStrings(SNIPPET, STATISTICS));

            youtubeApiUtil.setYouTubeRequest(videoList);
            videoList.setId(videoIds);

            return videoList.execute().getItems();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    public List<Video> getVideoListResponseByChannelId(String channelId, long maxResult) {
        PlaylistItemListResponse playlistItemListResponse = getVideosByChannelId(channelId, maxResult);
        List<String> videoIds = convertToVideoIds(playlistItemListResponse);

        return getVideosByIds(videoIds);
    }

    public VideoPlayerDetailResponse getVideoDetailForVideoPlayer(String videoId) {
        try {
            YouTube.Videos.List videoList = youTube.videos().list(convertToPartStrings(SNIPPET, STATISTICS));
            youtubeApiUtil.setYouTubeRequest(videoList);
            videoList.setId(List.of(videoId));

            Video video = videoList.execute().getItems().get(0);
            Channel channel = getChannelById(video.getSnippet().getChannelId());

            return VideoPlayerDetailResponse.from(video, channel);
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    public WithPageTokenResponse<List<CommentWithRepliesResponse>> getCommentsWithReplies(String videoId,
                                                                                          String pageToken) {
        try {
            YouTube.CommentThreads.List commentThreadsList = youTube.commentThreads()
                    .list(convertToPartStrings(ID, SNIPPET, REPLIES));
            youtubeApiUtil.setYouTubeRequest(commentThreadsList);
            commentThreadsList.setVideoId(videoId);
            commentThreadsList.setTextFormat("plainText");
            commentThreadsList.setPageToken(pageToken);

            CommentThreadListResponse commentThreadListResponse = commentThreadsList.execute();
            List<CommentWithRepliesResponse> data = commentThreadListResponse.getItems().stream()
                    .map(CommentWithRepliesResponse::from)
                    .toList();
            PageTokenResponse pageTokenResponse = new PageTokenResponse(null,
                    commentThreadListResponse.getNextPageToken());

            return new WithPageTokenResponse<>(data, pageTokenResponse);
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    private PlaylistItemListResponse getVideosByChannelId(String channelId, long maxResult) {
        return getVideosByChannelId(channelId, maxResult, null);
    }

    public PlaylistItemListResponse getVideosByChannelId(String channelId, long maxResult, String pageToken) {
        try {

            YouTube.PlaylistItems.List playlistItmesList = youTube.playlistItems().list(convertToPartStrings(SNIPPET));

            youtubeApiUtil.setYouTubeRequest(playlistItmesList);
            playlistItmesList.setPlaylistId(convertToUploadPlaylistId(channelId));
            playlistItmesList.setMaxResults(maxResult);
            playlistItmesList.setPageToken(pageToken);

            return playlistItmesList.execute();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }
}
