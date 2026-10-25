package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.UserRepository;
import com.expensetracker.dto.LoginDTO;
import com.expensetracker.dto.RegisterDTO;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.entity.User;
import com.expensetracker.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO register(RegisterDTO registerDTO) {
        log.debug("Starting registration for email: {}", registerDTO.getEmail());

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("Registration failed, email already exists: {}", registerDTO.getEmail());
            throw new RuntimeException("User Email Already Exist");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());

        User savedUser = userRepository.save(user);
        log.info("New user saved. id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return convertToUserDTO(savedUser);   // use savedUser, not user
    }

    @Override
    public UserDTO login(LoginDTO loginDTO) {
        log.debug("Starting login for email: {}", loginDTO.getEmail());

        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed, no user found for email: {}", loginDTO.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        if (!loginDTO.getPassword().equals(user.getPassword())) {
            log.warn("Login failed, wrong password for email: {}", loginDTO.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        log.info("Login validated for user id: {}", user.getId());
        return convertToUserDTO(user);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}