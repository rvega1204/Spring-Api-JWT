package com.rvg.store.dtos;

public record LoginRequest(
        String email,
        String password
) {}