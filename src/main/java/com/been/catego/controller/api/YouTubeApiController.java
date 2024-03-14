package com.been.catego.controller.api;

import com.been.catego.dto.response.CommentWithRepliesResponse;
import com.been.catego.dto.response.DataWithPageTokenResponse;
import com.been.catego.dto.response.SubscriptionChannelResponse;
import com.been.catego.service.YouTubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<DataWithPageTokenResponse<List<SubscriptionChannelResponse>>> getSubscriptions(
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(youTubeApiService.getSubscriptionsWithPageToken(pageToken, 50));
    }

    @GetMapping("/{videoId}/comments")
    public ResponseEntity<DataWithPageTokenResponse<List<CommentWithRepliesResponse>>> getComments(
            @PathVariable String videoId,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(youTubeApiService.getCommentsWithReplies(videoId, pageToken));
    }
}
