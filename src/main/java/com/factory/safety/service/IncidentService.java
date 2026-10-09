package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IncidentService {
    Incident saveIncident(Incident incident);
    List<Incident> getAllIncidents();
    Incident getIncidentById(Long id);
    Incident updateIncidentStatus(Long id, Status status);
    
    Incident createIncidentWithPhoto(Incident incident, MultipartFile photo, String cameraPhotoBase64);
    Incident resolveIncident(Long id, Status status, String resolutionNotes, String resolvedBy, MultipartFile resolutionPhoto, String cameraPhotoBase64);
    void deleteIncident(Long id);
    List<Incident> getIncidentsByReportedBy(String reportedBy);

    List<Incident> searchIncidents(Status status, Severity severity, String location);
    long countOpenIncidents();
    long countClosedIncidents();
    long countUnresolvedHighSeverity();
}
