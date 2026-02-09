package com.redditclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SubredditDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Subreddit name is required")
        @Size(min = 3, max = 50, message = "Name must be 3-50 characters")
        private String name;

        @Size(max = 500, message = "Description can be at most 500 characters")
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 500)
        private String description;
        private String bannerUrl;
        private String iconUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String bannerUrl;
        private String iconUrl;
        private Integer memberCount;
        private String creatorUsername;
        private Long creatorId;
        private Boolean isMember;
        private String createdAt;
    }
}
