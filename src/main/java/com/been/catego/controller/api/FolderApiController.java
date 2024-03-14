package com.been.catego.controller.api;

import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.response.SubscriptionChannelResponse;
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

    @GetMapping("/{folderId}/channels")
    public ResponseEntity<List<SubscriptionChannelResponse>> getAllSubscriptionsInFolderWithInclusionStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long folderId) {
        return ResponseEntity.ok(
                folderService.getAllSubscriptionsInFolder(folderId, principalDetails.getId()));
    }
}
