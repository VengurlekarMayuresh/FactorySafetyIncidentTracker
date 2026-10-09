package com.factory.safety.service;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import com.factory.safety.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private FileStorageService fileStorageService;
    private NotificationService notificationService;

    @Autowired
    public IncidentServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Autowired
    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Incident saveIncident(Incident incident) {
        Incident saved = incidentRepository.save(incident);
        if (notificationService != null) {
            notificationService.notifyAdmins(
                "New Incident Reported: #" + saved.getId() + " - " + saved.getTitle(),
                "Reported by " + saved.getReportedBy() + " at " + saved.getLocation() + " (Severity: " + saved.getSeverity() + ")",
                saved.getId(),
                "NEW_INCIDENT"
            );
        }
        return saved;
    }

    @Override
    public Incident createIncidentWithPhoto(Incident incident, MultipartFile photo, String cameraPhotoBase64) {
        if (fileStorageService != null) {
            if (photo != null && !photo.isEmpty()) {
                incident.setPhotoUrl(fileStorageService.storeFile(photo));
            } else if (cameraPhotoBase64 != null && !cameraPhotoBase64.trim().isEmpty()) {
                incident.setPhotoUrl(fileStorageService.storeBase64Image(cameraPhotoBase64));
            }
        }
        return saveIncident(incident);
    }

    @Override
    public Incident resolveIncident(Long id, Status status, String resolutionNotes, String resolvedBy, MultipartFile resolutionPhoto, String cameraPhotoBase64) {
        Incident incident = getIncidentById(id);
        incident.setStatus(status);
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            incident.setResolutionNotes(resolutionNotes.trim());
        }
        if (resolvedBy != null && !resolvedBy.trim().isEmpty()) {
            incident.setResolvedBy(resolvedBy.trim());
        }

        if (fileStorageService != null) {
            if (resolutionPhoto != null && !resolutionPhoto.isEmpty()) {
                incident.setResolutionPhotoUrl(fileStorageService.storeFile(resolutionPhoto));
            } else if (cameraPhotoBase64 != null && !cameraPhotoBase64.trim().isEmpty()) {
                incident.setResolutionPhotoUrl(fileStorageService.storeBase64Image(cameraPhotoBase64));
            }
        }

        Incident saved = incidentRepository.save(incident);

        if (notificationService != null && saved.getReportedBy() != null) {
            String noteText = saved.getResolutionNotes() != null ? " Note: " + saved.getResolutionNotes() : "";
            notificationService.notifyUser(
                saved.getReportedBy(),
                "Complaint #" + saved.getId() + " Status Updated: " + status,
                "Admin " + (resolvedBy != null ? resolvedBy : "") + " updated status to " + status + "." + noteText,
                saved.getId(),
                "STATUS_UPDATE"
            );
        }

        return saved;
    }

    @Override
    public void deleteIncident(Long id) {
        incidentRepository.deleteById(id);
    }

    @Override
    public List<Incident> getIncidentsByReportedBy(String reportedBy) {
        return incidentRepository.findByReportedByOrderByReportedAtDesc(reportedBy);
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
        Incident saved = incidentRepository.save(incident);
        if (notificationService != null && saved.getReportedBy() != null) {
            notificationService.notifyUser(
                saved.getReportedBy(),
                "Complaint #" + saved.getId() + " Status Updated: " + status,
                "Status has been changed to " + status,
                saved.getId(),
                "STATUS_UPDATE"
            );
        }
        return saved;
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
