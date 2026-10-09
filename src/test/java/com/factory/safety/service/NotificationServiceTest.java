package com.factory.safety.service;

import com.factory.safety.model.Notification;
import com.factory.safety.model.Role;
import com.factory.safety.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNotifyAdmins() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = notificationService.notifyAdmins("New Incident", "Hazard near bay 1", 1L, "NEW_INCIDENT");

        assertNotNull(notif);
        assertEquals(Role.ADMIN, notif.getTargetRole());
        assertEquals("New Incident", notif.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyUser() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification notif = notificationService.notifyUser("worker1", "Status Update", "Work completed", 1L, "STATUS_UPDATE");

        assertNotNull(notif);
        assertEquals("worker1", notif.getRecipientUsername());
        assertEquals(Role.USER, notif.getTargetRole());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead() {
        Notification notif = new Notification("worker1", Role.USER, "Title", "Msg", 1L, "NEW_INCIDENT");
        notif.setId(5L);
        notif.setRead(false);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(5L);

        assertTrue(notif.isRead());
        verify(notificationRepository, times(1)).save(notif);
    }
}
