package com.expensetracker.controller;

import com.expensetracker.dto.AuthResponseDTO;
import com.expensetracker.dto.LoginDTO;
import com.expensetracker.dto.RegisterDTO;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.security.JwtService;
import com.expensetracker.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterDTO registerDTO) {
        log.info("Register request received for email: {}", registerDTO.getEmail());
        UserDTO user = authService.register(registerDTO);
        log.info("User registered successfully with id: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        log.info("Login request received for email: {}", loginDTO.getEmail());

        UserDTO user = authService.login(loginDTO);

        String token = jwtService.generateToken(user);
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setExpiresAt(jwtService.extractExpiration(token).getTime());
        response.setUser(user);

        log.info("Login successful for user id: {}", user.getId());
        return ResponseEntity.ok(response);
    }
}