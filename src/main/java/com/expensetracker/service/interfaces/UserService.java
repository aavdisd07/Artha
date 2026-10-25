package com.expensetracker.service.interfaces;

import com.expensetracker.dto.ChangePasswordDTO;
import com.expensetracker.dto.UserDTO;

public interface UserService {

    UserDTO getProfile(Long userId);

    // Updates username and email (the id inside the DTO is ignored)
    UserDTO updateProfile(Long userId, UserDTO userDTO);

    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}