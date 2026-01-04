package com.example.library.dto;

import com.example.library.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String uid;
    private String name;
    private String surname;
    private String email;
    private String address;
    private String city;
    private Role role;
    private String token;
}
