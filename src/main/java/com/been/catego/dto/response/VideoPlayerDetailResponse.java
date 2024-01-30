package com.been.catego.dto.response;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import static com.been.catego.util.YoutubeFormatUtils.formatDateTime;
import static com.been.catego.util.YoutubeFormatUtils.formatSubscriberCount;
import static com.been.catego.util.YoutubeFormatUtils.formatViewCount;

public record VideoPlayerDetailResponse(
        String videoId,
        String videoName,
        String videoDescription,
        String channelName,
        String subscriberCount,
        String viewCount,
        String commentCount,
        String channelThumbnailUrl,
        String publishedAt
) {

    public static VideoPlayerDetailResponse from(Video video, Channel channel) {
        VideoSnippet videoSnippet = video.getSnippet();
        VideoStatistics videoStatistics = video.getStatistics();

        String formattedViewCount = formatViewCount(videoStatistics.getViewCount());
        String formattedPublishedAt = formatDateTime(videoSnippet.getPublishedAt());

        return new VideoPlayerDetailResponse(
                video.getId(),
                videoSnippet.getTitle(),
                formatDescription(videoSnippet.getDescription(), formattedViewCount, formattedPublishedAt),
                videoSnippet.getChannelTitle(),
                formatSubscriberCount(channel.getStatistics().getSubscriberCount()),
                formattedViewCount,
                String.valueOf(videoStatistics.getCommentCount()),
                channel.getSnippet().getThumbnails().getHigh().getUrl(),
                formattedPublishedAt
        );
    }

    private static String formatDescription(String description, String viewCount, String publishedAt) {
        return "조회수 " + viewCount + "회 " + publishedAt + "\n" + description;
    }
}
