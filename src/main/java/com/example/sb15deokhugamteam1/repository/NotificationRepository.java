package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationQueryRepository {

    long countByUserId(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Notification n
               set n.confirmed = true, n.updatedAt = :now
             where n.user.id = :userId and n.confirmed = false
            """)
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") Instant now);
}
