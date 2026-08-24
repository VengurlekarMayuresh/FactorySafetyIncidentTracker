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

    @InjectMocks
    private IncidentServiceImpl incidentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
}
