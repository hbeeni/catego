package com.been.catego.dto.response;

import com.been.catego.domain.Channel;
import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;

import java.util.List;

public record FolderInfoWithChannelInfoResponse(
        Long folderId,
        String folderName,
        List<ChannelInfoResponse> channels
) {

    public static FolderInfoWithChannelInfoResponse from(Folder folder) {
        return new FolderInfoWithChannelInfoResponse(folder.getId(),
                folder.getName(),
                folder.getFolderChannels().stream()
                        .map(ChannelInfoResponse::from)
                        .toList());
    }

    public record ChannelInfoResponse(
            String channelId,
            String channelTitle
    ) {

        public static ChannelInfoResponse from(FolderChannel folderChannel) {
            Channel channel = folderChannel.getChannel();
            return new ChannelInfoResponse(channel.getId(), channel.getName());
        }
    }
}
