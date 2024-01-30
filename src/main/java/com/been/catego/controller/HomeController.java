package com.been.catego.controller;

import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.response.ChannelWithFolderNamesResponse;
import com.been.catego.dto.response.WithPageTokenResponse;
import com.been.catego.service.ChannelService;
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

    private final ChannelService channelService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal PrincipalDetails principalDetails,
                       @RequestParam(required = false) String pageToken,
                       Model model) {
        WithPageTokenResponse<List<ChannelWithFolderNamesResponse>> result =
                channelService.findSubscriptionChannelsWithFolderNames(principalDetails.getId(), pageToken, 10);

        model.addAttribute("channels", result.data());
        model.addAttribute("pageToken", result.pageToken());

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
