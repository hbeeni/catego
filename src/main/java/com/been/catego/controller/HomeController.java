package com.been.catego.controller;

import com.been.catego.dto.PrincipalDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        model.addAttribute("profileImageUrl", principalDetails.getProfileImageUrl());
        model.addAttribute("nickname", principalDetails.getNickname());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
