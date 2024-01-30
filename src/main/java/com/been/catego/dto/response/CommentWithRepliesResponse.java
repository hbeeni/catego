package com.been.catego.dto.response;

import com.been.catego.util.YoutubeFormatUtils;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadReplies;

import java.util.List;

public record CommentWithRepliesResponse(
        CommentResponse comment,
        List<CommentResponse> replies
) {

    public static CommentWithRepliesResponse of(CommentResponse comment, List<CommentResponse> replies) {
        return new CommentWithRepliesResponse(comment, replies);
    }

    public static CommentWithRepliesResponse from(CommentThread commentThread) {
        CommentSnippet topLevelComment = commentThread.getSnippet().getTopLevelComment().getSnippet();

        CommentResponse comment = CommentResponse.of(
                topLevelComment.getAuthorDisplayName(),
                topLevelComment.getAuthorProfileImageUrl(),
                topLevelComment.getTextOriginal(),
                YoutubeFormatUtils.formatDateTime(topLevelComment.getPublishedAt()),
                topLevelComment.getLikeCount()
        );

        CommentThreadReplies commentThreadRelies = commentThread.getReplies();
        if (commentThreadRelies == null) {
            return CommentWithRepliesResponse.of(comment, null);
        }

        List<CommentResponse> replies = commentThreadRelies.getComments().stream()
                .map(reply -> {
                    CommentSnippet replySnippet = reply.getSnippet();
                    return CommentResponse.of(
                            replySnippet.getAuthorDisplayName(),
                            replySnippet.getAuthorProfileImageUrl(),
                            replySnippet.getTextOriginal(),
                            YoutubeFormatUtils.formatDateTime(replySnippet.getPublishedAt()),
                            replySnippet.getLikeCount());
                })
                .toList();

        return CommentWithRepliesResponse.of(comment, replies);
    }
}
