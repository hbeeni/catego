package com.been.catego.controller.api;

import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.response.SubscriptionResponse;
import com.been.catego.dto.response.VideoResponse;
import com.been.catego.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/folders")
@RestController
public class FolderApiController {

    private final FolderService folderService;

    @GetMapping("/{folderId}")
    public ResponseEntity<List<VideoResponse>> getFolderVideos(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(folderService.getFolderVideos(principalDetails.getId(), folderId));
    }

    @GetMapping("/{folderId}/channels")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptionsInFolderWithInclusionStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(
                folderService.getAllSubscriptionsWithInclusionStatusInFolder(folderId, principalDetails.getId()));
    }
}
