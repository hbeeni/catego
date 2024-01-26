package com.been.catego.dto.response;

public record WithPageTokenResponse<T>(
        T data,
        PageTokenResponse pageToken
) {
}
