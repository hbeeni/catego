package com.been.catego.dto.request;

import java.util.List;

public record CreateListRequest(
        String listName,
        List<String> channels
) {
}
