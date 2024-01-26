package com.been.catego.controller.api;

import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.response.ChannelResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
@RestController
public class SubscriptionApiController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<WithPageTokenResponse<List<ChannelResponse>>> getSubscriptions(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) String pageToken) {
        WithPageTokenResponse<List<ChannelResponse>> response =
                channelService.findAllSubscription(principalDetails.getId(), pageToken, 50);
        return ResponseEntity.ok(response);
    }
}
