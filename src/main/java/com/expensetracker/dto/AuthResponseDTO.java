package com.expensetracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDTO {
    private String token;
    private long expiresAt;   // expiry time in epoch milliseconds
    private UserDTO user;
}