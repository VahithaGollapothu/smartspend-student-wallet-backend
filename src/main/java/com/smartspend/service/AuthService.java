package com.smartspend.service;

import com.smartspend.dto.LoginRequest;
import com.smartspend.model.*;
import com.smartspend.repository.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public Map<String, Object> login(LoginRequest request) {
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword()))
            throw new RuntimeException("Invalid email or password");

        return buildResponse(student, generateToken(student));
    }

    public Map<String, Object> register(String name, String email, String password, String studentId) {
        if (studentRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Student student = studentRepository.save(Student.builder()
                .name(name).email(email)
                .password(passwordEncoder.encode(password))
                .studentId(studentId).build());

        walletRepository.save(Wallet.builder()
                .balance(BigDecimal.ZERO).student(student).build());

        return buildResponse(student, generateToken(student));
    }

    private Map<String, Object> buildResponse(Student student, String token) {
        return Map.of(
            "token", token,
            "studentId", student.getId(),
            "name", student.getName(),
            "email", student.getEmail()
        );
    }

    private String generateToken(Student student) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(student.getEmail())
                .claim("studentDbId", student.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            System.out.println("extractEmail failed: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("Token valid ✅");
            return true;
        } catch (Exception e) {
            System.out.println("Token invalid ❌: " + e.getMessage());
            return false;
        }
    }
}