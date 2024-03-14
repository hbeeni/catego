package com.been.catego.dto.response;

import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class SubscriptionChannelResponse {

    private final String channelId;
    private final String channelTitle;
    private final String thumbnailUrl;
    private boolean includedInFolder;

    public static SubscriptionChannelResponse from(Subscription subscription) {
        SubscriptionSnippet snippet = subscription.getSnippet();

        return new SubscriptionChannelResponse(
                snippet.getResourceId().getChannelId(),
                snippet.getTitle(),
                snippet.getThumbnails().getDefault().getUrl(),
                false
        );
    }

    public void setIncludedInFolderTrue() {
        includedInFolder = true;
    }
}
