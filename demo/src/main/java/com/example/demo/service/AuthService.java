package com.example.demo.service;

import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.domain.dto.SignUpResponse;
import com.example.demo.domain.entity.ResetToken;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.dto.AuthRequest;
import com.example.demo.persistence.TokenRepository;
import com.example.demo.persistence.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import com.example.demo.security.JwtUtil;
import org.apache.coyote.BadRequestException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, TokenRepository tokenRepository, JavaMailSender javaMailSender,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = javaMailSender;
        this.jwtUtil = jwtUtil;
    }

    private void sendEmail(String email, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText("Click here to reset password: " + resetLink);

        mailSender.send(message);
    }

    public void createForgotPasswordRequest(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();

        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(resetToken);
        sendEmail(user.getEmail(),token);
    }

    public void resetPassword(String token, String newPassword) {
        ResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    public SignUpResponse register(SignUpRequest request) {
        if(!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if(userRepository.findByUsername(request.username()).isPresent()){
            throw new RuntimeException("Username already exists");
        }
        if(request.BirthDate() == null){
            throw new RuntimeException("Date is required");
        }
        if(!request.BirthDate().isBefore(LocalDate.now())){
            throw new RuntimeException("Date is invalid");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBirthDate(request.BirthDate());

        User u = userRepository.save(user);
        return new SignUpResponse(
                u.getId().toString(),
                u.getEmail(),
                u.getUsername(),
                u.getBirthDate(),
                "User registered succesfully"
        );
    }

    public User login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

}
