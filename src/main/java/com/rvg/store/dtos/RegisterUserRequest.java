package com.rvg.store.dtos;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String password;
    private String email;
    private String name;
}
