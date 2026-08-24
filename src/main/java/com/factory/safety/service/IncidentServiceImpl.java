package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import com.factory.safety.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Autowired
    public IncidentServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public Incident saveIncident(Incident incident) {
        return incidentRepository.save(incident);
    }

    @Override
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @Override
    public Incident getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found with id: " + id));
    }

    @Override
    public Incident updateIncidentStatus(Long id, Status status) {
        Incident incident = getIncidentById(id);
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    @Override
    public List<Incident> searchIncidents(Status status, Severity severity, String location) {
        return incidentRepository.findFilteredIncidents(status, severity, location);
    }

    @Override
    public long countOpenIncidents() {
        return incidentRepository.countByStatus(Status.OPEN);
    }

    @Override
    public long countClosedIncidents() {
        return incidentRepository.countByStatus(Status.CLOSED);
    }

    @Override
    public long countUnresolvedHighSeverity() {
        return incidentRepository.countBySeverityAndStatusNot(Severity.HIGH, Status.CLOSED);
    }
}
