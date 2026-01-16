package com.rvg.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rvg.store.dtos.ChangePasswordRequest;
import com.rvg.store.dtos.RegisterUserRequest;
import com.rvg.store.dtos.UpdateUserRequest;
import com.rvg.store.dtos.UserDto;
import com.rvg.store.entities.User;
import com.rvg.store.mappers.UserMapper;
import com.rvg.store.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .build();

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setName("John Doe");
        testUserDto.setEmail("john@example.com");
    }

    @Test
    @DisplayName("GET /users - Should return all users sorted by name")
    void testGetAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll(Sort.by("name"))).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].email", is("john@example.com")));

        verify(userRepository, times(1)).findAll(Sort.by("name"));
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    @DisplayName("GET /users?sort=email - Should return users sorted by email")
    void testGetAllUsersSortedByEmail() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll(Sort.by("email"))).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/users")
                        .param("sort", "email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userRepository, times(1)).findAll(Sort.by("email"));
    }

    @Test
    @DisplayName("GET /users?sort=invalid - Should default to name sorting")
    void testGetAllUsersInvalidSort() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll(Sort.by("name"))).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/users")
                        .param("sort", "invalid"))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).findAll(Sort.by("name"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should return user")
    void testGetUser_Success() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /users/{id} - Should return 404")
    void testGetUser_NotFound() throws Exception {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("POST /users - Should create user and encrypt password")
    void testCreateUser_Success() throws Exception {
        // Given
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("plainPassword");

        User newUser = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("plainPassword")
                .build();

        UserDto newUserDto = new UserDto();
        newUserDto.setId(2L);
        newUserDto.setName("Jane Doe");
        newUserDto.setEmail("jane@example.com");

        when(userMapper.toEntity(any(RegisterUserRequest.class))).thenReturn(newUser);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(userMapper.toDto(any(User.class))).thenReturn(newUserDto);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Jane Doe")));

        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("PUT /users/{id} - Should update user")
    void testUpdateUser_Success() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Updated");
        request.setEmail("john.updated@example.com");

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setName("John Updated");
        updatedDto.setEmail("john.updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userMapper).update(any(UpdateUserRequest.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("PUT /users/{id} - Should return 500 when user not found")
    void testUpdateUser_NotFound() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Test");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("DELETE /users/{id} - Should delete user")
    void testDeleteUser_Success() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When & Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("DELETE /users/{id} - Should return 404")
    void testDeleteUser_NotFound() throws Exception {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("POST /users/{id}/change-password - Should return 401 when old password incorrect")
    void testChangePassword_WrongOldPassword() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/users/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(passwordEncoder, times(1)).matches("wrongPassword", testUser.getPassword());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /users/{id}/change-password - Should return 500 when user not found")
    void testChangePassword_UserNotFound() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("new");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/users/999/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(userRepository, times(1)).findById(999L);
    }
}