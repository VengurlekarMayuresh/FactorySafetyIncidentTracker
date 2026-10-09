package com.factory.safety.service;

import com.factory.safety.model.Notification;
import com.factory.safety.model.Role;
import java.util.List;

public interface NotificationService {
    Notification notifyAdmins(String title, String message, Long incidentId, String type);
    Notification notifyUser(String username, String title, String message, Long incidentId, String type);
    List<Notification> getNotificationsForUser(String username, Role role);
    long getUnreadCount(String username, Role role);
    void markAsRead(Long notificationId);
    void markAllAsRead(String username, Role role);
}
