package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
