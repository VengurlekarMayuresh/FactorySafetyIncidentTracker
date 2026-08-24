package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import java.util.List;

public interface IncidentService {
    Incident saveIncident(Incident incident);
    List<Incident> getAllIncidents();
    Incident getIncidentById(Long id);
    Incident updateIncidentStatus(Long id, Status status);
    
    List<Incident> searchIncidents(Status status, Severity severity, String location);
    long countOpenIncidents();
    long countClosedIncidents();
    long countUnresolvedHighSeverity();
}
