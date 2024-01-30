package com.been.catego.dto.response;

import com.been.catego.domain.Folder;

public record FolderResponse(
        Long folderId,
        String folderName,
        int channelCount
) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(folder.getId(), folder.getName(), folder.getFolderChannels().size());
    }
}
