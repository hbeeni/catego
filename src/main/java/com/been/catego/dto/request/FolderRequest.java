package com.been.catego.dto.request;

import java.util.List;

public record FolderRequest(
        String folderName,
        List<String> channels
) {
}
