package com.redditclone.dto;

import com.redditclone.model.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 300, message = "Title must be 1-300 characters")
        private String title;

        private String content;
        private String url;
        private String imageUrl;

        @NotNull(message = "Post type is required")
        private PostType postType;

        @NotNull(message = "Subreddit ID is required")
        private Long subredditId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String content;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String url;
        private String imageUrl;
        private PostType postType;
        private Integer voteCount;
        private Integer commentCount;
        private Boolean isLocked;
        private String subredditName;
        private Long subredditId;
        private String authorUsername;
        private Long authorId;
        private String authorAvatarUrl;
        private Integer userVote; // 1, -1, or 0
        private String createdAt;
        private String updatedAt;
    }
}
