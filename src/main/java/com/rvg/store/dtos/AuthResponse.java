package com.rvg.store.dtos;

public record AuthResponse(
        String token,
        String email,
        String name
) {}