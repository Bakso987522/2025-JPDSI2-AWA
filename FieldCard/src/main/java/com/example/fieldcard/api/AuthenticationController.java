package com.example.fieldcard.api;

import com.example.fieldcard.core.security.service.AuthenticationService;
import com.example.fieldcard.dto.auth.AuthenticationRequest;
import com.example.fieldcard.dto.auth.AuthenticationResponse;
import com.example.fieldcard.dto.auth.RegisterRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent
    ) {
        return ResponseEntity.ok(service.register(request, userAgent));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent
    ) {
        return ResponseEntity.ok(service.authenticate(request, userAgent));
    }
}