package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.UserRepository;
import com.expensetracker.dto.ChangePasswordDTO;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72; // BCrypt only uses the first 72 bytes
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile(Long userId) {
        return toDTO(findUser(userId));
    }

    @Override
    @Transactional
    public UserDTO updateProfile(Long userId, UserDTO dto) {
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim();

        if (username.length() < 3 || username.length() > 50) {
            throw new BadRequestException("Username must be between 3 and 50 characters");
        }
        if (email.isEmpty() || email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Please enter a valid email address");
        }

        User user = findUser(userId);

        // Another user must not already have this username or email
        userRepository.findByUsername(username)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> { throw new BadRequestException("Username is already taken"); });

        userRepository.findByEmail(email)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> { throw new BadRequestException("Email is already in use"); });

        user.setUsername(username);
        user.setEmail(email);

        User saved = userRepository.save(user); // UPDATE users SET username=?, email=? WHERE id=?
        log.info("Profile updated for user id: {}", userId);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        String current = dto.getCurrentPassword();
        String next = dto.getNewPassword();

        if (current == null || current.isEmpty()) {
            throw new BadRequestException("Current password is required");
        }
        if (next == null || next.length() < MIN_PASSWORD_LENGTH || next.length() > MAX_PASSWORD_LENGTH) {
            throw new BadRequestException("New password must be between "
                    + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters");
        }

        User user = findUser(userId);

        if (!passwordEncoder.matches(current, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(next, user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(next));
        userRepository.save(user);
        log.info("Password changed for user id: {}", userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}