package com.been.catego.service;

import com.been.catego.domain.Folder;
import com.been.catego.domain.FolderChannel;
import com.been.catego.dto.ChannelResponse;
import com.been.catego.dto.CommonResponse;
import com.been.catego.dto.PageTokenResponse;
import com.been.catego.repository.FolderChannelRepository;
import com.been.catego.repository.FolderRepository;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class FolderChannelService {

    private final FolderRepository folderRepository;
    private final FolderChannelRepository folderChannelRepository;
    private final YouTubeService youtubeService;

    public CommonResponse<List<ChannelResponse>> findAllSubscription(Long userId, String pageToken) {
        SubscriptionListResponse subscriptionListResponse = youtubeService.getSubscriptionListResponse(pageToken);
        List<String> subscriptionIds = youtubeService.getSubscriptionIds(subscriptionListResponse);
        List<Channel> channels = youtubeService.getChannels(subscriptionIds);

        List<Folder> folders = folderRepository.findByUser_Id(userId);
        Map<String, List<FolderChannel>> channelIdToFolderChannelMap =
                getChannelIdToFolderChannelMap(toFolderIds(folders));

        List<ChannelResponse> data = channels.stream()
                .map(channel -> ChannelResponse.from(channel, channelIdToFolderChannelMap.get(channel.getId())))
                .sorted(Comparator.comparing(ChannelResponse::channelTitle))
                .toList();
        PageTokenResponse pageTokenResponse = new PageTokenResponse(subscriptionListResponse.getPrevPageToken(),
                subscriptionListResponse.getNextPageToken());

        return CommonResponse.of(data, pageTokenResponse);
    }

    private List<Long> toFolderIds(List<Folder> folders) {
        return folders.stream()
                .map(Folder::getId)
                .toList();
    }

    private Map<String, List<FolderChannel>> getChannelIdToFolderChannelMap(List<Long> folderIds) {
        return folderChannelRepository.findAllByFolderIdIn(folderIds).stream()
                .collect(Collectors.groupingBy(fc -> fc.getChannel().getId()));
    }
}
