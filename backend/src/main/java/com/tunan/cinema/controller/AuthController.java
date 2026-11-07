package com.tunan.cinema.controller;

import com.tunan.cinema.auth.AuthContext;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.common.PublicApi;
import com.tunan.cinema.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PublicApi
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        String username = text(body.get("username"));
        String password = text(body.get("password"));
        if (username.isEmpty() || password.isEmpty()) throw new BizException("请输入账号和密码");
        return service.login(username, password);
    }

    @PublicApi
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        return service.registerUser(body);
    }

    @PublicApi
    @PostMapping("/register-cinema")
    public Map<String, Object> registerCinema(@RequestBody Map<String, Object> body) {
        return service.registerCinema(body);
    }

    @GetMapping("/me")
    public Map<String, Object> me() { return service.me(AuthContext.get().id()); }

    @PutMapping("/profile")
    public Map<String, Object> profile(@RequestBody Map<String, Object> body) {
        return service.updateProfile(AuthContext.get().id(), body);
    }

    @PutMapping("/password")
    public Map<String, Object> password(@RequestBody Map<String, Object> body) {
        service.changePassword(AuthContext.get().id(), text(body.get("currentPassword")), text(body.get("newPassword")));
        return Map.of("message", "密码修改成功");
    }

    private String text(Object value) { return value == null ? "" : String.valueOf(value); }
}
