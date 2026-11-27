package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.request.UserLoginRequest;
import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.dto.request.UserUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserRegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(loginRequest));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> find(@PathVariable UUID userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(
            @PathVariable UUID userId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(userService.update(userId, requestUserId, updateRequest));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDelete(
            @PathVariable UUID userId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        userService.softDelete(userId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDelete(
            @PathVariable UUID userId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        userService.hardDelete(userId, requestUserId);
        return ResponseEntity.noContent().build();
    }
}
