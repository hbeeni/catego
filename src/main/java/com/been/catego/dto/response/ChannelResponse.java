package com.been.catego.dto.response;

import com.been.catego.util.YoutubeFormatUtils;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;

public record ChannelResponse(
        String channelId,
        String channelTitle,
        String customUrl,
        String channelDescription,
        String thumbnailUrl,
        String subscriberCount,
        String videoCount
) {

    public static ChannelResponse from(Channel channel) {
        ChannelSnippet snippet = channel.getSnippet();
        ChannelStatistics statistics = channel.getStatistics();

        return new ChannelResponse(
                channel.getId(),
                snippet.getTitle(),
                snippet.getCustomUrl(),
                snippet.getDescription(),
                snippet.getThumbnails().getDefault().getUrl(),
                YoutubeFormatUtils.formatSubscriberCount(statistics.getSubscriberCount()),
                YoutubeFormatUtils.formatVideoCount(statistics.getVideoCount())
        );
    }
}
