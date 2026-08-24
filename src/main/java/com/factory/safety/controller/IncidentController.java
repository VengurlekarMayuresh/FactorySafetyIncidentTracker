package com.factory.safety.controller;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
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
    public String listIncidents(Model model) {
        model.addAttribute("incidents", incidentService.getAllIncidents());
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
        return "incidents/detail";
    }
}
