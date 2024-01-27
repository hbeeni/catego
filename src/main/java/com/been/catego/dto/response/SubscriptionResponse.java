package com.been.catego.dto.response;

import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class SubscriptionResponse {

    private final String channelId;
    private final String channelTitle;
    private final String thumbnailUrl;
    private boolean includedInFolder;

    public static SubscriptionResponse from(Subscription subscription) {
        SubscriptionSnippet snippet = subscription.getSnippet();

        return new SubscriptionResponse(
                snippet.getResourceId().getChannelId(),
                snippet.getTitle(),
                snippet.getThumbnails().getDefault().getUrl(),
                false
        );
    }

    public void setIncludedInFolderTrue() {
        includedInFolder = true;
    }

    @Override
    public String toString() {
        return "SubscriptionResponse[" +
                "channelId=" + channelId + ", " +
                "channelTitle=" + channelTitle + ", " +
                "thumbnailUrl=" + thumbnailUrl + ", " +
                "isIncludedInFolder=" + includedInFolder + ']';
    }
}
