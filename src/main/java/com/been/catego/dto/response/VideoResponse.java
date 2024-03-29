package com.been.catego.dto.response;

import com.been.catego.util.YoutubeFormatUtils;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;

public record VideoResponse(
        String videoId,
        String videoName,
        String viewCount,
        String videoThumbnailUrl,
        String publishedAt
) {

    public static VideoResponse from(Video video) {
        VideoSnippet snippet = video.getSnippet();

        return new VideoResponse(
                video.getId(),
                snippet.getTitle(),
                YoutubeFormatUtils.formatViewCount(video.getStatistics().getViewCount()),
                snippet.getThumbnails().getHigh().getUrl(),
                YoutubeFormatUtils.formatDateTime(snippet.getPublishedAt())
        );
    }
}
