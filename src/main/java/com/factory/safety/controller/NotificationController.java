package com.factory.safety.controller;

import com.factory.safety.model.User;
import com.factory.safety.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/read-all")
    public String markAllAsRead(HttpSession session, HttpServletRequest request) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            notificationService.markAllAsRead(user.getUsername(), user.getRole());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/incidents");
    }
}
