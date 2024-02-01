package com.been.catego.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum YouTubePart {

    ID("id"),
    REPLIES("replies"),
    SNIPPET("snippet"),
    STATISTICS("statistics");

    private final String partString;

    public static List<String> convertToPartStrings(YouTubePart... parts) {
        return Arrays.stream(parts).map(YouTubePart::getPartString).toList();
    }
}
