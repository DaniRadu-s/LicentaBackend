package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request){
        authService.createForgotPasswordRequest(request.email());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request){
        authService.resetPassword(request.token(), request.newPassword());
    }
    @PostMapping("/register")
    public ResponseEntity<SignUpResponse> register(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        User user = authService.login(request);

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token,new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(), user.getLastName(),
                user.getBirthDate()
        ) );
    }
}
