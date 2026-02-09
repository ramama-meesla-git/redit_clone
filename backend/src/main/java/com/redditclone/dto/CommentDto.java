package com.redditclone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class CommentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Comment content is required")
        private String content;
        private Long parentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "Comment content is required")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private Integer voteCount;
        private Integer depth;
        private String authorUsername;
        private Long authorId;
        private String authorAvatarUrl;
        private Long postId;
        private Long parentId;
        private Integer userVote;
        private String createdAt;
        private String updatedAt;

        @Builder.Default
        private List<Response> children = new ArrayList<>();
    }
}
