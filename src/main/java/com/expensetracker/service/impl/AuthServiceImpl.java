package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.UserRepository;
import com.expensetracker.dto.LoginDTO;
import com.expensetracker.dto.RegisterDTO;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72; // BCrypt only uses the first 72 bytes

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO register(RegisterDTO registerDTO) {
        log.debug("Starting registration for email: {}", registerDTO.getEmail());

        if (isBlank(registerDTO.getUsername()) || isBlank(registerDTO.getEmail()) || isBlank(registerDTO.getPassword())) {
            throw new BadRequestException("Username, email and password are required");
        }
        String password = registerDTO.getPassword();
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be between "
                    + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters");
        }

        String username = registerDTO.getUsername().trim();
        String email = registerDTO.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed, email already exists: {}", email);
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed, username already exists: {}", username);
            throw new BadRequestException("Username is already taken");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));   // store the BCrypt hash, never the plain password

        User savedUser = userRepository.save(user);
        log.info("New user saved. id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return convertToUserDTO(savedUser);
    }

    @Override
    public UserDTO login(LoginDTO loginDTO) {
        log.debug("Starting login for email: {}", loginDTO.getEmail());

        if (isBlank(loginDTO.getEmail()) || isBlank(loginDTO.getPassword())) {
            throw new BadRequestException("Email and password are required");
        }

        User user = userRepository.findByEmail(loginDTO.getEmail().trim())
                .orElseThrow(() -> {
                    log.warn("Login failed, no user found for email: {}", loginDTO.getEmail());
                    return new BadRequestException("Invalid email or password");
                });

        String stored = user.getPassword();
        boolean legacyPlainText = !isBcryptHash(stored);

        boolean passwordOk = legacyPlainText
                ? loginDTO.getPassword().equals(stored)                 // old accounts created before BCrypt
                : passwordEncoder.matches(loginDTO.getPassword(), stored);

        if (!passwordOk) {
            log.warn("Login failed, wrong password for email: {}", loginDTO.getEmail());
            throw new BadRequestException("Invalid email or password");
        }

        // An old plain-text password was just proven correct, so replace it with a hash now
        if (legacyPlainText) {
            user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
            userRepository.save(user);
            log.info("Upgraded password to BCrypt for user id: {}", user.getId());
        }

        log.info("Login validated for user id: {}", user.getId());
        return convertToUserDTO(user);
    }

    // BCrypt hashes look like $2a$10$... (60 characters)
    private boolean isBcryptHash(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}