package com.been.catego.service;

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

import static com.been.catego.service.YouTubePart.ID;
import static com.been.catego.service.YouTubePart.REPLIES;
import static com.been.catego.service.YouTubePart.SNIPPET;
import static com.been.catego.service.YouTubePart.STATISTICS;
import static com.been.catego.service.YouTubePart.convertToPartStrings;
import static com.been.catego.util.YoutubeConvertUtils.convertToUploadPlaylistId;

@RequiredArgsConstructor
@Service
public class YouTubeApiDataService {

    private static final YouTube youTube;
    private final YouTubeApiUtil youtubeApiUtil;

    static {
        youTube = YouTubeApiUtil.youTube();
    }

    /**
     * 로그인한 사용자가 구독한 유튜브 채널을 모두 반환한다.
     */
    public List<Subscription> getAllSubscriptionChannels() {
        List<Subscription> subscriptions = new ArrayList<>();
        String nextPageToken = null;

        do {
            SubscriptionListResponse subscriptionListResponse = getSubscriptionListResponse(nextPageToken, 50L);
            subscriptions.addAll(subscriptionListResponse.getItems());
            nextPageToken = subscriptionListResponse.getNextPageToken();
        } while (nextPageToken != null);

        return subscriptions;
    }

    /**
     * 구독한 채널을 알파벳 순으로 반환한다.
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

    /**
     * 구독 채널로부터 채널 정보를 반환한다.
     *
     * @param subscriptionListResponse 구독 채널 리스트
     */
    public List<Channel> getChannels(SubscriptionListResponse subscriptionListResponse) {
        List<String> subscriptionChannelIds = YoutubeConvertUtils.convertToSubscriptionIds(subscriptionListResponse);
        return getChannelsByIds(subscriptionChannelIds);
    }

    /**
     * 해당 channelId의 채널 정보를 반환한다.
     */
    public Channel getChannelById(String channelId) {
        Channel channel = getChannelsByIds(List.of(channelId)).get(0);

        if (channel == null) {
            throw new CustomException(ErrorMessages.NOT_FOUND_CHANNEL);
        }

        return channel;
    }

    /**
     * 해당 channelId의 채널 정보를 반환한다.
     */
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

    /**
     * 해당 videoId의 유튜브 동영상을 반환한다.
     */
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

    /**
     * 해당 videoId의 유튜브 동영상을 반환한다.
     */
    public Video getVideoById(String videoId) {
        Video video;

        try {
            YouTube.Videos.List videoList = youTube.videos().list(convertToPartStrings(SNIPPET, STATISTICS));
            youtubeApiUtil.setYouTubeRequest(videoList);
            videoList.setId(List.of(videoId));

            video = videoList.execute().getItems().get(0);
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }

        if (video == null) {
            throw new CustomException(ErrorMessages.NOT_FOUND_VIDEO);
        }

        return video;
    }

    public CommentThreadListResponse getCommentThreadListResponseByVideoId(String videoId, String pageToken) {
        try {
            YouTube.CommentThreads.List commentThreadsList = youTube.commentThreads()
                    .list(convertToPartStrings(ID, SNIPPET, REPLIES));
            youtubeApiUtil.setYouTubeRequest(commentThreadsList);
            commentThreadsList.setVideoId(videoId);
            commentThreadsList.setTextFormat("plainText");
            commentThreadsList.setPageToken(pageToken);

            return commentThreadsList.execute();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }

    /**
     * 해당 채널의 업로드 동영상 재생목록의 동영상 리스트를 반환한다.
     */
    public PlaylistItemListResponse getVideosByChannelId(String channelId, long maxResult, String pageToken) {
        try {
            YouTube.PlaylistItems.List playlistItemsList = youTube.playlistItems().list(convertToPartStrings(SNIPPET));

            youtubeApiUtil.setYouTubeRequest(playlistItemsList);
            playlistItemsList.setPlaylistId(convertToUploadPlaylistId(channelId));
            playlistItemsList.setMaxResults(maxResult);
            playlistItemsList.setPageToken(pageToken);

            return playlistItemsList.execute();
        } catch (GoogleJsonResponseException e) {
            throw new CustomException(e.getMessage());
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.FAIL_TO_LOAD_YOUTUBE_DATA);
        }
    }
}
