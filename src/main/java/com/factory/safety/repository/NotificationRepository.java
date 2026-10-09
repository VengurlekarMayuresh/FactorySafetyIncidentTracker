package com.factory.safety.repository;

import com.factory.safety.model.Notification;
import com.factory.safety.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE (n.recipientUsername = :username OR n.targetRole = :role) ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsForUser(@Param("username") String username, @Param("role") Role role);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.recipientUsername = :username OR n.targetRole = :role) AND n.isRead = false")
    long countUnreadForUser(@Param("username") String username, @Param("role") Role role);

    List<Notification> findByTargetRoleOrderByCreatedAtDesc(Role role);
}
