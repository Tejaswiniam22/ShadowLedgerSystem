package com.shadowledger.gateway.controller;

import com.shadowledger.gateway.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/token")
    public Map<String, String> token(@RequestParam String role) {
        return Map.of(
                "token", JwtUtil.generateToken(role)
        );
    }
}

