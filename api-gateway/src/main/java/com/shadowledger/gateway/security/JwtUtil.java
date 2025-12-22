package com.shadowledger.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(
                    "shadow-ledger-secret-key-256-bit-minimum".getBytes()
            );

    public static String generateToken(String role) {
        return Jwts.builder()
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 60 * 60 * 1000)
                )
                .signWith(KEY)
                .compact();
    }

    public static SecretKey getKey() {
        return KEY;
    }
}

