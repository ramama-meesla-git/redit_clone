package com.redditclone.service;

import com.redditclone.dto.NotificationDto;
import com.redditclone.exception.ResourceNotFoundException;
import com.redditclone.model.Notification;
import com.redditclone.model.User;
import com.redditclone.repository.NotificationRepository;
import com.redditclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(Long userId, Long triggeredById, String type, String message,
            Long entityId, String entityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User triggeredBy = triggeredById != null ? userRepository.findById(triggeredById).orElse(null) : null;

        // Don't notify yourself
        if (triggeredById != null && triggeredById.equals(userId))
            return;

        Notification notification = Notification.builder()
                .user(user)
                .triggeredBy(triggeredBy)
                .type(type)
                .message(message)
                .entityId(entityId)
                .entityType(entityType)
                .build();

        notification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationDto.Response response = mapToResponse(notification);
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                response);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        notifications.forEach(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationDto.Response mapToResponse(Notification notification) {
        return NotificationDto.Response.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .entityId(notification.getEntityId())
                .entityType(notification.getEntityType())
                .isRead(notification.getIsRead())
                .triggeredByUsername(
                        notification.getTriggeredBy() != null ? notification.getTriggeredBy().getUsername() : null)
                .createdAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null)
                .build();
    }
}
