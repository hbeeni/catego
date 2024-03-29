package com.been.catego.controller;

import com.been.catego.dto.ChannelDto;
import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.request.FolderRequest;
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
                               @ModelAttribute FolderRequest request) {

        Map<String, ChannelDto> channelIdToChannelDtoMap = parseChannelString(request);
        folderService.createFolder(principalDetails.getId(), request.folderName(), channelIdToChannelDtoMap);
        return "redirect:/";
    }

    @GetMapping("/{folderId}/edit")
    public String editForm(@AuthenticationPrincipal PrincipalDetails principalDetails,
                           @PathVariable Long folderId,
                           Model model) {
        model.addAttribute("folderId", folderId);
        model.addAttribute("folder", folderService.getFolderInfo(folderId, principalDetails.getId()));
        return "folder/edit-folder-form";
    }

    @PostMapping("/{folderId}/edit")
    public String editFolder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                             @PathVariable Long folderId,
                             @ModelAttribute FolderRequest request) {

        Map<String, ChannelDto> channelIdToChannelDtoMap = parseChannelString(request);
        folderService.editFolder(folderId, principalDetails.getId(), request.folderName(), channelIdToChannelDtoMap);
        return "redirect:/";
    }

    @PostMapping("/{folderId}/delete")
    public String deleteFolder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                               @PathVariable Long folderId) {
        folderService.deleteFolder(folderId, principalDetails.getId());
        return "redirect:/";
    }

    private static Map<String, ChannelDto> parseChannelString(FolderRequest request) {
        return request.channels().stream()
                .map(channel -> channel.split("\\|")) // |로 분할
                .collect(toMap(
                        splitChannel -> splitChannel[0],
                        splitChannel -> new ChannelDto(splitChannel[0], splitChannel[1])
                ));
    }
}
