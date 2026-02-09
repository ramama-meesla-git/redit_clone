package com.redditclone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class VoteDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "Vote type is required (1 or -1)")
        private Short voteType; // 1 = upvote, -1 = downvote

        private Long postId;
        private Long commentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long entityId;
        private String entityType;
        private Integer voteCount;
        private Short userVote;
    }
}
