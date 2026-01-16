package com.rvg.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rvg.store.dtos.LoginRequest;
import com.rvg.store.dtos.RegisterUserRequest;
import com.rvg.store.entities.User;
import com.rvg.store.repositories.UserRepository;
import com.rvg.store.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterUserRequest registerRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        loginRequest = new LoginRequest("test@example.com", "password123");

        registerRequest = new RegisterUserRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("newpassword");

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities("USER")
                .build();
    }

    @Test
    @DisplayName("POST /auth/login - Should login successfully and return JWT token")
    void testLogin_Success() throws Exception {
        // Given
        String expectedToken = "jwt-token-12345";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(loginRequest.email()))
                .thenReturn(userDetails);
        when(jwtService.generateToken(userDetails))
                .thenReturn(expectedToken);
        when(userRepository.findByEmail(loginRequest.email()))
                .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(expectedToken)))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.name", is(testUser.getName())));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(loginRequest.email());
        verify(jwtService, times(1)).generateToken(userDetails);
        verify(userRepository, times(1)).findByEmail(loginRequest.email());
    }

    @Test
    @DisplayName("POST /auth/register - Should register new user successfully")
    void testRegister_Success() throws Exception {
        // Given
        String expectedToken = "new-jwt-token-67890";
        String encodedPassword = "encodedNewPassword";

        User newUser = User.builder()
                .id(2L)
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(encodedPassword)
                .build();

        UserDetails newUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(newUser.getEmail())
                .password(newUser.getPassword())
                .authorities("USER")
                .build();

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(newUser);
        when(userDetailsService.loadUserByUsername(registerRequest.getEmail()))
                .thenReturn(newUserDetails);
        when(jwtService.generateToken(newUserDetails))
                .thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(expectedToken)))
                .andExpect(jsonPath("$.email", is(registerRequest.getEmail())))
                .andExpect(jsonPath("$.name", is(registerRequest.getName())));

        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userDetailsService, times(1)).loadUserByUsername(registerRequest.getEmail());
        verify(jwtService, times(1)).generateToken(newUserDetails);
    }

    @Test
    @DisplayName("POST /auth/register - Should hash password before saving")
    void testRegister_PasswordIsEncoded() throws Exception {
        // Given
        String rawPassword = "plainPassword123";
        String encodedPassword = "encoded_hash_xyz";

        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("User");
        request.setEmail("user@test.com");
        request.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("token");

        // When
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(argThat(user ->
                user.getPassword().equals(encodedPassword)
        ));
    }
}