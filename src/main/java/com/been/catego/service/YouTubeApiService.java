package com.been.catego.service;

import com.been.catego.dto.response.CommentWithRepliesResponse;
import com.been.catego.dto.response.DataWithPageTokenResponse;
import com.been.catego.dto.response.PageTokenResponse;
import com.been.catego.dto.response.SubscriptionChannelResponse;
import com.been.catego.dto.response.VideoPlayerDetailResponse;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class YouTubeApiService {

    private final YouTubeApiDataService youTubeApiDataService;

    public DataWithPageTokenResponse<List<SubscriptionChannelResponse>> getSubscriptionsWithPageToken(String pageToken,
                                                                                                      long maxResult) {
        SubscriptionListResponse subscriptionListResponse =
                youTubeApiDataService.getSubscriptionListResponse(pageToken, maxResult);

        PageTokenResponse pageTokenResponse = new PageTokenResponse(subscriptionListResponse.getPrevPageToken(),
                subscriptionListResponse.getNextPageToken());

        List<SubscriptionChannelResponse> data = subscriptionListResponse.getItems().stream()
                .map(SubscriptionChannelResponse::from)
                .toList();

        return new DataWithPageTokenResponse<>(data, pageTokenResponse);
    }

    public VideoPlayerDetailResponse getVideoDetailForVideoPlayer(String videoId) {
        Video video = youTubeApiDataService.getVideoById(videoId);
        Channel channel = youTubeApiDataService.getChannelById(video.getSnippet().getChannelId());

        return VideoPlayerDetailResponse.from(video, channel);
    }

    public DataWithPageTokenResponse<List<CommentWithRepliesResponse>> getCommentsWithReplies(String videoId,
                                                                                              String pageToken) {
        CommentThreadListResponse commentThreadListResponse
                = youTubeApiDataService.getCommentThreadListResponseByVideoId(videoId, pageToken);

        List<CommentWithRepliesResponse> data = commentThreadListResponse.getItems().stream()
                .map(CommentWithRepliesResponse::from)
                .toList();

        PageTokenResponse pageTokenResponse = new PageTokenResponse(null, commentThreadListResponse.getNextPageToken());

        return new DataWithPageTokenResponse<>(data, pageTokenResponse);
    }
}
