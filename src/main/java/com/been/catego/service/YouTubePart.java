package com.been.catego.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum YouTubePart {

    CONTENT_DETAILS("contentDetails"),
    ID("id"),
    PLAYER("player"),
    SNIPPET("snippet"),
    STATISTICS("statistics"),
    SUBSCRIBER_SNIPPET("subscriberSnippet");

    private final String partString;

    public static List<String> convertToPartStrings(YouTubePart... parts) {
        return Arrays.stream(parts).map(YouTubePart::getPartString).toList();
    }
}
