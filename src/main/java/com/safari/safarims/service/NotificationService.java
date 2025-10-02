package com.safari.safarims.service;

import com.safari.safarims.entity.Notification;
import com.safari.safarims.entity.User;
import com.safari.safarims.common.enums.UserRole;
import com.safari.safarims.repository.NotificationRepository;
import com.safari.safarims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyUser(Long userId, String type, String title, String body) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .body(body)
            .build();

        notificationRepository.save(notification);
        log.info("Notification sent to user {}: {}", user.getUsername(), title);
    }

    @Transactional
    public void notifyBookingOfficers(String title, String body) {
        List<User> bookingOfficers = userRepository.findByRole(UserRole.BOOKING_OFFICER);
        bookingOfficers.forEach(user -> notifyUser(user.getId(), "BOOKING", title, body));
    }

    @Transactional
    public void notifyCrewManagers(String title, String body) {
        List<User> crewManagers = userRepository.findByRole(UserRole.TOUR_CREW_MANAGER);
        crewManagers.forEach(user -> notifyUser(user.getId(), "ALLOCATION", title, body));
    }

    @Transactional
    public void notifyMaintenanceOfficers(String title, String body) {
        List<User> maintenanceOfficers = userRepository.findByRole(UserRole.MAINTENANCE_OFFICER);
        maintenanceOfficers.forEach(user -> notifyUser(user.getId(), "MAINTENANCE", title, body));
    }

    @Transactional
    public void notifyDriversAndGuides(String title, String body) {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        List<User> guides = userRepository.findByRole(UserRole.GUIDE);

        drivers.forEach(user -> notifyUser(user.getId(), "ALLOCATION", title, body));
        guides.forEach(user -> notifyUser(user.getId(), "ALLOCATION", title, body));
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only mark your own notifications as read");
        }

        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(notification -> {
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }
}
