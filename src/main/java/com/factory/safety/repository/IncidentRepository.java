package com.factory.safety.repository;

import com.factory.safety.model.Incident;
import com.factory.safety.model.Severity;
import com.factory.safety.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    long countByStatus(Status status);

    long countBySeverityAndStatusNot(Severity severity, Status status);

    @Query("SELECT i FROM Incident i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:severity IS NULL OR i.severity = :severity) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Incident> findFilteredIncidents(
        @Param("status") Status status,
        @Param("severity") Severity severity,
        @Param("location") String location
    );

    List<Incident> findByReportedByOrderByReportedAtDesc(String reportedBy);
}
