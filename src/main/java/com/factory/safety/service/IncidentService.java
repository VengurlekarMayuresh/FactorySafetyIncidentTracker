package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Status;
import java.util.List;

public interface IncidentService {
    Incident saveIncident(Incident incident);
    List<Incident> getAllIncidents();
    Incident getIncidentById(Long id);
    Incident updateIncidentStatus(Long id, Status status);
}
