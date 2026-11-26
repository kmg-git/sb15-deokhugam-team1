package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
