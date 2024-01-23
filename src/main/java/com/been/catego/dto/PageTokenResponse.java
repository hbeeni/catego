package com.been.catego.dto;

public record PageTokenResponse(
        String prevPageToken,
        String nextPageToken
) {
}
