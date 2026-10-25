package com.expensetracker.controller;

import com.expensetracker.dto.ChangePasswordDTO;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal Long userId) {
        log.info("Fetch profile request for user id: {}", userId);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserDTO userDTO) {
        log.info("Update profile request for user id: {}", userId);
        return ResponseEntity.ok(userService.updateProfile(userId, userDTO));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("Change password request for user id: {}", userId);
        userService.changePassword(userId, changePasswordDTO);
        return ResponseEntity.noContent().build();
    }
}