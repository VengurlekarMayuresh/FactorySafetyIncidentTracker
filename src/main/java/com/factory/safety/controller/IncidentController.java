package com.factory.safety.controller;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import com.factory.safety.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @Autowired
    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public String listIncidents(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String location,
            Model model) {
        
        if (status != null || severity != null || (location != null && !location.trim().isEmpty())) {
            model.addAttribute("incidents", incidentService.searchIncidents(status, severity, location));
        } else {
            model.addAttribute("incidents", incidentService.getAllIncidents());
        }

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

        return "incidents/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("incident", new Incident());
        model.addAttribute("severities", Severity.values());
        return "incidents/create";
    }

    @PostMapping
    public String saveIncident(@ModelAttribute("incident") Incident incident) {
        incidentService.saveIncident(incident);
        return "redirect:/incidents";
    }

    @GetMapping("/{id}")
    public String viewIncident(@PathVariable Long id, Model model) {
        model.addAttribute("incident", incidentService.getIncidentById(id));
        model.addAttribute("statuses", Status.values());
        return "incidents/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Status status) {
        incidentService.updateIncidentStatus(id, status);
        return "redirect:/incidents/" + id;
    }
}
