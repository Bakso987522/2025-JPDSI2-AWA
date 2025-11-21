package com.example.fieldcard.core.security.service;

import com.example.fieldcard.data.entity.AuthProvider;
import com.example.fieldcard.data.entity.User;
import com.example.fieldcard.data.repository.UserRepository;
import com.example.fieldcard.dto.auth.AuthenticationRequest;
import com.example.fieldcard.dto.auth.AuthenticationResponse;
import com.example.fieldcard.dto.auth.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request, String userAgent) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setProvider(AuthProvider.LOCAL);

        repository.save(user);

        String jwtToken = jwtService.generateToken(user, userAgent);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, String userAgent) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user, userAgent);
        return new AuthenticationResponse(jwtToken);
    }
}