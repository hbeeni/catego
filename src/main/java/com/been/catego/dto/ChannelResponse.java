package com.been.catego.dto;

import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.util.FormatUtil;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;

import java.util.List;

public record ChannelResponse(
        String channelId,
        String channelTitle,
        String customUrl,
        String channelDescription,
        String thumbnailUrl,
        String subscriberCount,
        List<String> folderNames
) {

    public static ChannelResponse from(Channel channel, List<FolderChannel> folderChannels) {
        ChannelSnippet snippet = channel.getSnippet();
        ChannelStatistics statistics = channel.getStatistics();

        return new ChannelResponse(
                channel.getId(),
                snippet.getTitle(),
                snippet.getCustomUrl(),
                snippet.getDescription(),
                snippet.getThumbnails().getDefault().getUrl(),
                FormatUtil.formatSubscriberCount(statistics.getSubscriberCount()),
                folderChannels == null ? null : folderChannels.stream()
                        .map(FolderChannel::getFolder)
                        .map(Folder::getName)
                        .toList()
        );
    }
}
