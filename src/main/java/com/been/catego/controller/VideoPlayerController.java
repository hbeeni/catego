package com.been.catego.controller;

import com.been.catego.dto.response.VideoPlayerDetailResponse;
import com.been.catego.service.YouTubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RequestMapping("/watch")
@Controller
public class VideoPlayerController {

    private final YouTubeApiService youTubeApiService;

    @GetMapping
    public String videoPlayer(@RequestParam String videoId, Model model) {
        VideoPlayerDetailResponse response = youTubeApiService.getVideoDetailForVideoPlayer(videoId);
        model.addAttribute("videoPlayer", response);
        return "folder/video-player";
    }
}
