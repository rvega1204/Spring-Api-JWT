package com.rvg.store.controllers;

import com.rvg.store.dtos.ChangePasswordRequest;
import com.rvg.store.dtos.RegisterUserRequest;
import com.rvg.store.dtos.UpdateUserRequest;
import com.rvg.store.dtos.UserDto;
import com.rvg.store.mappers.UserMapper;
import com.rvg.store.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

/**
 * REST controller for managing users.
 * Provides endpoints for user registration, retrieval, and profile updates.
 */

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users, optionally sorted by name or email.
     *
     * @param sort optional sort parameter (default: name)
     * @return a list of {@link UserDto} objects
     */
    @GetMapping
    public Iterable<UserDto> getAllUsers(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sort) {
        if (!Set.of("name", "email").contains(sort))
            sort = "name";

        return userRepository.findAll(Sort.by(sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id the ID of the user
     * @return a {@link ResponseEntity} containing the {@link UserDto} if found,
     *         or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    /**
     * Creates a new user.
     * Password is automatically encrypted using BCrypt before saving.
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody RegisterUserRequest request,
            UriComponentsBuilder builder) {
        var user = userMapper.toEntity(request);

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.save(user);

        var uri = builder.path("/users/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(userMapper.toDto(user));
    }

    /**
     * Updates an existing user.
     * Note: Use change-password endpoint to update passwords.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userMapper.update(request, user);
        user = userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     * @return a {@link ResponseEntity} with 204 No Content if successful, or 404
     *         Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        userRepository.delete(user);

        return ResponseEntity.ok().build();
    }

    /**
     * Changes a user's password.
     *
     * @param id      the ID of the user
     * @param request the new password data
     * @return a {@link ResponseEntity} with 204 No Content if successful, or 401
     *         Unauthorized if old password is incorrect
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}
