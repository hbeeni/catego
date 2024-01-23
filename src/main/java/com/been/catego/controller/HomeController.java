package com.been.catego.controller;

import com.been.catego.dto.ChannelResponse;
import com.been.catego.dto.CommonResponse;
import com.been.catego.dto.PrincipalDetails;
import com.been.catego.service.FolderChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final FolderChannelService folderChannelService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal PrincipalDetails principalDetails,
                       @RequestParam(required = false) String pageToken,
                       Model model) {
        CommonResponse<List<ChannelResponse>> result = folderChannelService.findAllSubscription(
                principalDetails.getId(), pageToken);

        model.addAttribute("profileImageUrl", principalDetails.getProfileImageUrl());
        model.addAttribute("nickname", principalDetails.getNickname());
        model.addAttribute("channels", result.getData());
        model.addAttribute("pageToken", result.getPageToken());

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
