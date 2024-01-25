package com.been.catego.dto.response;

public record PageTokenResponse(
        String prevPageToken,
        String nextPageToken
) {
}
