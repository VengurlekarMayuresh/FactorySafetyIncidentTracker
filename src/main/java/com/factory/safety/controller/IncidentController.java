package com.factory.safety.controller;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Role;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import com.factory.safety.model.User;
import com.factory.safety.service.IncidentService;
import com.factory.safety.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final UserService userService;

    @Autowired
    public IncidentController(IncidentService incidentService, UserService userService) {
        this.incidentService = incidentService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping
    public String listIncidents(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String view,
            HttpSession session,
            Model model) {
        
        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Incident> incidents;
        if ("my".equalsIgnoreCase(view) && currentUser != null) {
            incidents = incidentService.getIncidentsByReportedBy(currentUser.getUsername());
        } else if (status != null || severity != null || (location != null && !location.trim().isEmpty())) {
            incidents = incidentService.searchIncidents(status, severity, location);
        } else {
            incidents = incidentService.getAllIncidents();
        }

        model.addAttribute("incidents", incidents);
        model.addAttribute("currentView", view);

        // Expose filters to form
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSeverity", severity);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("severities", Severity.values());

        // Expose counts/metrics
        model.addAttribute("openCount", incidentService.countOpenIncidents());
        model.addAttribute("closedCount", incidentService.countClosedIncidents());
        model.addAttribute("criticalCount", incidentService.countUnresolvedHighSeverity());

        // Expose worker/user specific counts
        if (currentUser != null) {
            List<Incident> myIncidents = incidentService.getIncidentsByReportedBy(currentUser.getUsername());
            long myOpen = myIncidents.stream().filter(i -> i.getStatus() != Status.CLOSED).count();
            long myClosed = myIncidents.stream().filter(i -> i.getStatus() == Status.CLOSED).count();
            model.addAttribute("myOpenCount", myOpen);
            model.addAttribute("myClosedCount", myClosed);
            model.addAttribute("myTotalCount", myIncidents.size());
        }

        return "incidents/list";
    }

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        Incident incident = new Incident();
        incident.setReportedBy(currentUser.getUsername());
        model.addAttribute("incident", incident);
        model.addAttribute("severities", Severity.values());
        return "incidents/create";
    }

    @PostMapping
    public String saveIncident(
            @ModelAttribute("incident") Incident incident,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "cameraPhotoBase64", required = false) String cameraPhotoBase64,
            HttpSession session) {

        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        incident.setReportedBy(currentUser.getUsername());
        incidentService.createIncidentWithPhoto(incident, photo, cameraPhotoBase64);
        return "redirect:/incidents";
    }

    @GetMapping("/{id}")
    public String viewIncident(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("incident", incidentService.getIncidentById(id));
        model.addAttribute("statuses", Status.values());
        return "incidents/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "newStatus", required = false) Status newStatus,
            @RequestParam(value = "resolutionNotes", required = false) String resolutionNotes,
            @RequestParam(value = "resolutionPhoto", required = false) MultipartFile resolutionPhoto,
            @RequestParam(value = "cameraResolutionBase64", required = false) String cameraResolutionBase64,
            HttpSession session) {

        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        Status targetStatus = status != null ? status : newStatus;
        if (targetStatus == null) {
            targetStatus = Status.OPEN;
        }

        incidentService.resolveIncident(id, targetStatus, resolutionNotes, currentUser.getUsername(), resolutionPhoto, cameraResolutionBase64);
        return "redirect:/incidents/" + id;
    }

    @PostMapping("/{id}/close")
    public String closeIncident(@PathVariable Long id, HttpSession session) {
        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        Incident incident = incidentService.getIncidentById(id);
        if (incident != null && (currentUser.isAdmin() || currentUser.getUsername().equalsIgnoreCase(incident.getReportedBy()))) {
            String actor = currentUser.isAdmin() ? "Safety Officer (" + currentUser.getUsername() + ")" : "Reporter (" + currentUser.getUsername() + ")";
            incidentService.resolveIncident(id, Status.CLOSED, "Request marked closed by " + actor, currentUser.getUsername(), null, null);
        }
        return "redirect:/incidents/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteIncident(@PathVariable Long id, HttpSession session) {
        User currentUser = getAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }
        Incident incident = incidentService.getIncidentById(id);
        if (incident != null && (currentUser.isAdmin() || currentUser.getUsername().equalsIgnoreCase(incident.getReportedBy()))) {
            incidentService.deleteIncident(id);
        }
        return "redirect:/incidents";
    }
}
