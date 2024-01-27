package com.been.catego.dto.response;

import com.been.catego.domain.Folder;
import com.been.catego.util.YoutubeFormatUtils;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;

import java.util.List;

public record ChannelWithFolderNamesResponse(
        String channelId,
        String channelTitle,
        String customUrl,
        String channelDescription,
        String thumbnailUrl,
        String subscriberCount,
        List<String> folderNames
) {

    public static ChannelWithFolderNamesResponse from(Channel channel, List<Folder> folders) {
        ChannelSnippet snippet = channel.getSnippet();
        ChannelStatistics statistics = channel.getStatistics();

        return new ChannelWithFolderNamesResponse(
                channel.getId(),
                snippet.getTitle(),
                snippet.getCustomUrl(),
                snippet.getDescription(),
                snippet.getThumbnails().getDefault().getUrl(),
                YoutubeFormatUtils.formatSubscriberCount(statistics.getSubscriberCount()),
                folders.isEmpty() ? null : folders.stream()
                        .map(Folder::getName)
                        .toList()
        );
    }
}
