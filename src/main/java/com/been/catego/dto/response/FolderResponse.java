package com.been.catego.dto.response;

import com.been.catego.domain.Folder;

public record FolderResponse(
        String folderName,
        int channelCount
) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(folder.getName(), folder.getFolderChannels().size());
    }
}
