package com.been.catego.controller;

import com.been.catego.dto.response.ChannelResponse;
import com.been.catego.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/channel")
@Controller
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping("/{channelId}")
    public String channel(@PathVariable String channelId, Model model) {
        ChannelResponse channel = channelService.getChannelInfo(channelId);
        model.addAttribute("channel", channel);
        return "channel/channel";
    }
}
