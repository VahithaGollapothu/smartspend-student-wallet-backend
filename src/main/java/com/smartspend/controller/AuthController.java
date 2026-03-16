package com.smartspend.controller;

import com.smartspend.dto.LoginRequest;
import com.smartspend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://smartspend-student-wallet-frontend.onrender.com")
public class AuthController {

	private final AuthService authService;
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {  // ← remove @Valid
	    try {
	        return ResponseEntity.ok(authService.login(request));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
	    }
	}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.register(
                    body.get("name"),
                    body.get("email"),
                    body.get("password"),
                    body.get("studentId")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}