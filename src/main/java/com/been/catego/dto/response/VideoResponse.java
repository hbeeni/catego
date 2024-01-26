package com.been.catego.dto.response;

import com.been.catego.util.YoutubeFormatUtils;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;

public record VideoResponse(
        String videoId,
        String videoName,
        String channelName,
        String viewCount,
        String videoThumbnailUrl,
        String channelThumbnailUrl,
        String publishedAt,
        String playerTag
) {

    public static VideoResponse from(Video video, Channel channel) {
        VideoSnippet snippet = video.getSnippet();

        return new VideoResponse(
                video.getId(),
                snippet.getTitle(),
                snippet.getChannelTitle(),
                YoutubeFormatUtils.formatViewCount(video.getStatistics().getViewCount()),
                snippet.getThumbnails().getHigh().getUrl(),
                channel.getSnippet().getThumbnails().getHigh().getUrl(),
                YoutubeFormatUtils.formatDateTime(snippet.getPublishedAt()),
                video.getPlayer().getEmbedHtml()
        );
    }
}
