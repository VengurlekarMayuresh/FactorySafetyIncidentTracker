package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import com.factory.safety.repository.IncidentRepository;
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

class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        incidentService.setFileStorageService(fileStorageService);
        incidentService.setNotificationService(notificationService);
    }

    @Test
    void testSaveIncident() {
        Incident incident = new Incident("Spill", "Acid spill in hall A", Severity.HIGH, "Hall A", "Worker 1");
        when(incidentRepository.save(incident)).thenReturn(incident);

        Incident saved = incidentService.saveIncident(incident);

        assertNotNull(saved);
        assertEquals("Spill", saved.getTitle());
        verify(incidentRepository, times(1)).save(incident);
    }

    @Test
    void testGetAllIncidents() {
        Incident incident1 = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        Incident incident2 = new Incident("Fire", "Small fire", Severity.HIGH, "Hall B", "Worker 2");
        when(incidentRepository.findAll()).thenReturn(Arrays.asList(incident1, incident2));

        List<Incident> list = incidentService.getAllIncidents();

        assertEquals(2, list.size());
        verify(incidentRepository, times(1)).findAll();
    }

    @Test
    void testGetIncidentById() {
        Incident incident = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Incident found = incidentService.getIncidentById(1L);

        assertNotNull(found);
        assertEquals("Spill", found.getTitle());
    }

    @Test
    void testUpdateIncidentStatus() {
        Incident incident = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        incident.setId(1L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident updated = incidentService.updateIncidentStatus(1L, Status.IN_PROGRESS);

        assertEquals(Status.IN_PROGRESS, updated.getStatus());
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void testSearchIncidents() {
        Incident incident = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        when(incidentRepository.findFilteredIncidents(Status.OPEN, Severity.HIGH, "Hall")).thenReturn(Arrays.asList(incident));

        List<Incident> searchResults = incidentService.searchIncidents(Status.OPEN, Severity.HIGH, "Hall");

        assertEquals(1, searchResults.size());
        verify(incidentRepository, times(1)).findFilteredIncidents(Status.OPEN, Severity.HIGH, "Hall");
    }

    @Test
    void testCounts() {
        when(incidentRepository.countByStatus(Status.OPEN)).thenReturn(5L);
        when(incidentRepository.countByStatus(Status.CLOSED)).thenReturn(3L);
        when(incidentRepository.countBySeverityAndStatusNot(Severity.HIGH, Status.CLOSED)).thenReturn(2L);

        assertEquals(5L, incidentService.countOpenIncidents());
        assertEquals(3L, incidentService.countClosedIncidents());
        assertEquals(2L, incidentService.countUnresolvedHighSeverity());
    }

    @Test
    void testCreateIncidentWithPhoto() {
        Incident incident = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        when(fileStorageService.storeBase64Image("data:image/jpeg;base64,sample")).thenReturn("/uploads/test.jpg");
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = incidentService.createIncidentWithPhoto(incident, null, "data:image/jpeg;base64,sample");

        assertNotNull(result);
        assertEquals("/uploads/test.jpg", result.getPhotoUrl());
        verify(notificationService, times(1)).notifyAdmins(anyString(), anyString(), any(), eq("NEW_INCIDENT"));
    }

    @Test
    void testResolveIncident() {
        Incident incident = new Incident("Spill", "Acid spill", Severity.HIGH, "Hall A", "Worker 1");
        incident.setId(10L);
        when(incidentRepository.findById(10L)).thenReturn(Optional.of(incident));
        when(fileStorageService.storeBase64Image("data:image/jpeg;base64,workdone")).thenReturn("/uploads/resolved.jpg");
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident resolved = incidentService.resolveIncident(10L, Status.CLOSED, "Cleaned and verified safe", "Admin John", null, "data:image/jpeg;base64,workdone");

        assertNotNull(resolved);
        assertEquals(Status.CLOSED, resolved.getStatus());
        assertEquals("Cleaned and verified safe", resolved.getResolutionNotes());
        assertEquals("Admin John", resolved.getResolvedBy());
        assertEquals("/uploads/resolved.jpg", resolved.getResolutionPhotoUrl());
        verify(notificationService, times(1)).notifyUser(eq("Worker 1"), anyString(), anyString(), eq(10L), eq("STATUS_UPDATE"));
    }
}
