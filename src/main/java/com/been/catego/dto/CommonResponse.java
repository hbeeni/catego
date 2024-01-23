package com.been.catego.dto;

import lombok.Getter;

@Getter
public class CommonResponse<T> {

    private final T data;
    private final PageTokenResponse pageToken;

    private CommonResponse(T data, PageTokenResponse pageToken) {
        this.data = data;
        this.pageToken = pageToken;
    }

    public static <T> CommonResponse<T> of(T data) {
        return new CommonResponse<>(data, null);
    }

    public static <T> CommonResponse<T> of(T data, PageTokenResponse pageToken) {
        return new CommonResponse<>(data, pageToken);
    }
}
