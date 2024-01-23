package com.been.catego.util;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class YouTubeApiUtil {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String APPLICATION_NAME = "CATEGO";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final OAuth2AuthorizedClientService clientService;

    public static YouTube youTube() {
        return new YouTube.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                request -> {
                })
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void setYouTubeRequest(YouTubeRequest<?> youTubeRequest) {
        youTubeRequest.setKey(apiKey);
        youTubeRequest.setAccessToken(getAccessToken());
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication.getClass().isAssignableFrom(OAuth2AuthenticationToken.class)) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

            OAuth2AuthorizedClient client =
                    clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
            System.out.println("OAuth2AuthorizedClient = " + client);

            if (client != null) {
                return client.getAccessToken().getTokenValue();
            }
        }

        return null;
    }
}
