package com.been.catego.controller.api;

import com.been.catego.dto.response.SubscriptionResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.service.YouTubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/youtube")
@RestController
public class YouTubeApiController {

    private final YouTubeApiService youTubeApiService;

    @GetMapping("/subscriptions")
    public ResponseEntity<WithPageTokenResponse<List<SubscriptionResponse>>> getSubscriptions(
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(youTubeApiService.getSubscriptionsWithPageToken(pageToken, 50));
    }
}
