package com.been.catego.controller;

import com.been.catego.dto.ChannelDto;
import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.request.CreateListRequest;
import com.been.catego.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/folder")
@Controller
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/new")
    public String newForm() {
        return "folder/create-folder-form";
    }

    @PostMapping("/new")
    public String createFolder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                               @ModelAttribute CreateListRequest request,
                               RedirectAttributes redirectAttributes) {
        log.info("request = {}", request);

        Map<String, ChannelDto> channelIdToChannelDtoMap = parseChannelString(request);

        Long folderId =
                folderService.createFolder(principalDetails.getId(), request.listName(), channelIdToChannelDtoMap);
        redirectAttributes.addAttribute("folderId", folderId);

        return "redirect:/folder/{folderId}";
    }

    @GetMapping("/{folderId}")
    public String getFolderMain(@PathVariable Long folderId, Model model) {
        model.addAttribute("folderId", folderId);
        return "folder/folder";
    }

    private static Map<String, ChannelDto> parseChannelString(CreateListRequest request) {
        return request.channels().stream()
                .map(channel -> channel.split("\\|")) // |로 분할
                .collect(toMap(
                        splitChannel -> splitChannel[0],
                        splitChannel -> new ChannelDto(splitChannel[0], splitChannel[1])
                ));
    }
}