package com.been.catego.service;

import com.been.catego.exception.CustomException;
import com.been.catego.exception.ErrorMessages;
import com.been.catego.util.YouTubeApiUtil;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class YouTubeService {

    private static final YouTube youTube;
    private final YouTubeApiUtil youtubeApiUtil;

    static {
        youTube = YouTubeApiUtil.youTube();
    }

    public SubscriptionListResponse getSubscriptionListResponse(String pageToken) {
        try {
            YouTube.Subscriptions.List subscriptionList = youTube.subscriptions().list(List.of("snippet"));

            youtubeApiUtil.setYouTubeRequest(subscriptionList);
            subscriptionList.setMine(true);
            subscriptionList.setOrder("alphabetical");
            subscriptionList.setMaxResults(50L);
            subscriptionList.setPageToken(pageToken);

            return subscriptionList.execute();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.IO_EXCEPTION);
        }
    }

    public List<String> getSubscriptionIds(SubscriptionListResponse subscriptionListResponse) {
        return subscriptionListResponse.getItems().stream()
                .map(Subscription::getSnippet)
                .map(SubscriptionSnippet::getResourceId)
                .map(ResourceId::getChannelId)
                .toList();
    }

    public List<Channel> getChannels(List<String> channelIds) {
        try {
            YouTube.Channels.List channelList = youTube.channels().list(List.of("snippet", "statistics"));

            youtubeApiUtil.setYouTubeRequest(channelList);
            channelList.setId(channelIds);

            return channelList.execute().getItems();
        } catch (IOException e) {
            throw new CustomException(ErrorMessages.IO_EXCEPTION);
        }
    }
}
