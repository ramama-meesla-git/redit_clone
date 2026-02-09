package com.redditclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotificationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String type;
        private String message;
        private Long entityId;
        private String entityType;
        private Boolean isRead;
        private String triggeredByUsername;
        private String createdAt;
    }
}
