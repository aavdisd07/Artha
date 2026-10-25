package com.expensetracker.service.interfaces;

import com.expensetracker.dto.LoginDTO;
import com.expensetracker.dto.RegisterDTO;
import com.expensetracker.dto.UserDTO;

public interface AuthService {
    UserDTO register(RegisterDTO registerDTO);
    UserDTO login(LoginDTO loginDTO);
}