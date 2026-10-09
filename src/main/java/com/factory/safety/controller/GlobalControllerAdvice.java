package com.factory.safety.controller;

import com.factory.safety.model.Notification;
import com.factory.safety.model.User;
import com.factory.safety.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @Autowired
    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @ModelAttribute("unreadNotificationsCount")
    public long getUnreadNotificationsCount(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return 0;
        }
        return notificationService.getUnreadCount(user.getUsername(), user.getRole());
    }

    @ModelAttribute("recentNotifications")
    public List<Notification> getRecentNotifications(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return Collections.emptyList();
        }
        return notificationService.getNotificationsForUser(user.getUsername(), user.getRole());
    }
}
