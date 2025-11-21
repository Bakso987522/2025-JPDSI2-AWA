package com.example.fieldcard.core.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String generateToken(UserDetails userDetails, String userAgent) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("fingerprint", generateFingerprint(userAgent));
        return buildToken(claims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, String currentUserAgent) {
        final String username = extractUsername(token);

        final String tokenFingerprint = extractClaim(token, claims -> claims.get("fingerprint", String.class));

        final String currentFingerprint = generateFingerprint(currentUserAgent);

        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && (tokenFingerprint != null && tokenFingerprint.equals(currentFingerprint));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateFingerprint(String userAgent) {
        try {
            if (userAgent == null) userAgent = "UNKNOWN";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] userAgentBytes = userAgent.getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = digest.digest(userAgentBytes);

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Błąd serwera: Brak algorytmu SHA-256", e);
        }
    }
}