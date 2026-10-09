package com.factory.safety.service;

import com.factory.safety.model.Notification;
import com.factory.safety.model.Role;
import com.factory.safety.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification notifyAdmins(String title, String message, Long incidentId, String type) {
        Notification notification = new Notification(null, Role.ADMIN, title, message, incidentId, type);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification notifyUser(String username, String title, String message, Long incidentId, String type) {
        Notification notification = new Notification(username, Role.USER, title, message, incidentId, type);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(String username, Role role) {
        if (role == Role.ADMIN) {
            return notificationRepository.findNotificationsForUser(username, Role.ADMIN);
        } else {
            return notificationRepository.findNotificationsForUser(username, null);
        }
    }

    @Override
    public long getUnreadCount(String username, Role role) {
        if (role == Role.ADMIN) {
            return notificationRepository.countUnreadForUser(username, Role.ADMIN);
        } else {
            return notificationRepository.countUnreadForUser(username, null);
        }
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsRead(String username, Role role) {
        List<Notification> list = getNotificationsForUser(username, role);
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
