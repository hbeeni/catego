package com.been.catego.dto.response;

public record DataWithPageTokenResponse<T>(
        T data,
        PageTokenResponse pageToken
) {
}
