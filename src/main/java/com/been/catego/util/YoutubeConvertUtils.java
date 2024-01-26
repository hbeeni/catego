package com.been.catego.util;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public final class YoutubeConvertUtils {

    private YoutubeConvertUtils() {
    }

    public static LocalDateTime convertToLocalDateTime(DateTime dateTime) {
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return LocalDateTime.ofInstant(instant, ZoneId.of("GMT"));
    }

    public static LocalDateTime convertToKoreaLocalDateTime(DateTime dateTime) {
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    public static List<String> convertToSubscriptionIds(SubscriptionListResponse subscriptionListResponse) {
        return subscriptionListResponse.getItems().stream()
                .map(Subscription::getSnippet)
                .map(SubscriptionSnippet::getResourceId)
                .map(ResourceId::getChannelId)
                .toList();
    }

    public static List<String> convertToVideoIds(SearchListResponse searchListResponse) {
        return searchListResponse.getItems().stream()
                .map(searchResult -> searchResult.getId().getVideoId())
                .toList();
    }
}
