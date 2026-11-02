package com.factory.safety.controller;

import com.factory.safety.model.User;
import com.factory.safety.service.SeleniumTestRunnerService;
import com.factory.safety.service.SeleniumTestRunnerService.TestRunResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SeleniumDashboardController {

    private final SeleniumTestRunnerService seleniumTestRunnerService;

    @Autowired
    public SeleniumDashboardController(SeleniumTestRunnerService seleniumTestRunnerService) {
        this.seleniumTestRunnerService = seleniumTestRunnerService;
    }

    @GetMapping("/selenium")
    public String showSeleniumDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("latestRun", seleniumTestRunnerService.getLatestRun());
        model.addAttribute("runHistory", seleniumTestRunnerService.getRunHistory());
        return "selenium/dashboard";
    }

    @RequestMapping(value = "/api/selenium/run", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<TestRunResult> triggerSeleniumSuite(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        String actor = (currentUser != null) ? currentUser.getUsername() : "mayu";
        TestRunResult result = seleniumTestRunnerService.runFullAutomation(actor);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/selenium/latest")
    @ResponseBody
    public ResponseEntity<TestRunResult> getLatestRun() {
        return ResponseEntity.ok(seleniumTestRunnerService.getLatestRun());
    }
}

