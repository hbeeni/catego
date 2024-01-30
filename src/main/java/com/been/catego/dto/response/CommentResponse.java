package com.been.catego.dto.response;

public record CommentResponse(
        String authorDisplayName,
        String authorProfileImageUrl,
        String comment,
        String publishedAt,
        long likeCount
) {

    public static CommentResponse of(String authorDisplayName, String authorProfileImageUrl, String comment,
                                     String publishedAt, long likeCount) {
        return new CommentResponse(authorDisplayName, authorProfileImageUrl, comment, publishedAt, likeCount);
    }
}
