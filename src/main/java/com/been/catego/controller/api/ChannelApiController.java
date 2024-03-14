package com.been.catego.controller.api;

import com.been.catego.dto.response.DataWithPageTokenResponse;
import com.been.catego.dto.response.VideoResponse;
import com.been.catego.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/channels")
@RestController
public class ChannelApiController {

    private final ChannelService channelService;

    @GetMapping("/{channelId}/videos")
    public ResponseEntity<DataWithPageTokenResponse<List<VideoResponse>>> getVideosForChannel(
            @PathVariable String channelId, @RequestParam(defaultValue = "20") int maxResult,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(channelService.getVideosOfChannel(channelId, maxResult, pageToken));
    }
}
