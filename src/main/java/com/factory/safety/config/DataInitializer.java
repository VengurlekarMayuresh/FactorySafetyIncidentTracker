package com.factory.safety.config;

import com.factory.safety.model.*;
import com.factory.safety.repository.IncidentRepository;
import com.factory.safety.repository.UserRepository;
import com.factory.safety.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            IncidentRepository incidentRepository,
            NotificationService notificationService) {
        return args -> {
            // Seed Admin User
            User admin = null;
            if (!userRepository.existsByUsername("admin")) {
                admin = new User("admin", "admin123", "Site Safety Director", Role.ADMIN, "EHS Department");
                userRepository.save(admin);
            }

            // Seed Worker User
            User worker = null;
            if (!userRepository.existsByUsername("worker1")) {
                worker = new User("worker1", "worker123", "Rajesh Sharma", Role.USER, "Machining Workshop");
                userRepository.save(worker);
            }

            // Seed sample incidents if none exist
            if (incidentRepository.count() == 0) {
                Incident inc1 = new Incident(
                    "Hydraulic oil leakage under CNC machine #3",
                    "Pool of hydraulic fluid spotted during morning shift inspection. High slipping risk for operators.",
                    Severity.HIGH,
                    "CNC Bay 3",
                    "worker1"
                );
                inc1.setStatus(Status.OPEN);
                incidentRepository.save(inc1);

                Incident inc2 = new Incident(
                    "Emergency exit signage light burnt out",
                    "Exit indicator lamp above door 4B is unlit during night shifts.",
                    Severity.MEDIUM,
                    "Building B Exit 4",
                    "worker1"
                );
                inc2.setStatus(Status.IN_PROGRESS);
                incidentRepository.save(inc2);

                Incident inc3 = new Incident(
                    "Exposed wiring near coolant dispenser",
                    "Frayed insulation on the 240V power cord near fluid station.",
                    Severity.HIGH,
                    "Coolant Station North",
                    "worker1"
                );
                inc3.setStatus(Status.CLOSED);
                inc3.setResolvedAt(LocalDateTime.now().minusHours(2));
                inc3.setResolvedBy("admin");
                inc3.setResolutionNotes("Cables replaced with industrial conduit and verified safe by electrical team.");
                incidentRepository.save(inc3);

                // Sample notifications
                notificationService.notifyAdmins(
                    "New Complaint: Hydraulic oil leakage under CNC machine #3",
                    "Submitted by worker1 at CNC Bay 3. Please inspect and resolve.",
                    inc1.getId(),
                    "NEW_INCIDENT"
                );

                notificationService.notifyUser(
                    "worker1",
                    "Complaint #3 Resolved",
                    "Admin resolved: Cables replaced with industrial conduit and verified safe.",
                    inc3.getId(),
                    "STATUS_UPDATE"
                );
            }
        };
    }
}
