package com.tunan.cinema.service;

import cn.hutool.crypto.digest.BCrypt;
import com.tunan.cinema.auth.AuthContext;
import com.tunan.cinema.auth.JwtService;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.AuthMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final JwtService jwtService;

    public AuthService(AuthMapper authMapper, JwtService jwtService) {
        this.authMapper = authMapper;
        this.jwtService = jwtService;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> user = authMapper.findByUsername(username);
        if (user == null || !BCrypt.checkpw(password, String.valueOf(user.get("password")))) {
            throw new BizException("账号或密码不正确", 401);
        }
        if (((Number) user.get("status")).intValue() != 1) {
            throw new BizException("账号已被停用，请联系平台管理员", 403);
        }
        return loginResult(user);
    }

    @Transactional
    public Map<String, Object> registerUser(Map<String, Object> body) {
        String username = text(body.get("username"));
        String password = text(body.get("password"));
        if (username.length() < 3) throw new BizException("账号至少需要3个字符");
        if (password.length() < 6) throw new BizException("密码至少需要6位");
        if (authMapper.countUsername(username) > 0) throw new BizException("该账号已被注册");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("password", BCrypt.hashpw(password));
        data.put("name", text(body.getOrDefault("name", username)));
        data.put("phone", text(body.get("phone")));
        data.put("email", text(body.get("email")));
        data.put("role", "USER");
        authMapper.insertUser(data);
        return loginResult(authMapper.findById(((Number) data.get("id")).longValue()));
    }

    @Transactional
    public Map<String, Object> registerCinema(Map<String, Object> body) {
        String username = text(body.get("username"));
        String password = text(body.get("password"));
        if (username.length() < 3) throw new BizException("账号至少需要3个字符");
        if (password.length() < 6) throw new BizException("密码至少需要6位");
        if (text(body.get("cinemaName")).isEmpty()) throw new BizException("请填写影院名称");
        if (text(body.get("address")).isEmpty()) throw new BizException("请填写影院地址");
        if (authMapper.countUsername(username) > 0) throw new BizException("该账号已被注册");

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", username);
        user.put("password", BCrypt.hashpw(password));
        user.put("name", text(body.getOrDefault("name", username)));
        user.put("phone", text(body.get("phone")));
        user.put("email", text(body.get("email")));
        user.put("role", "CINEMA");
        authMapper.insertUser(user);
        long uid = ((Number) user.get("id")).longValue();

        Map<String, Object> cinema = new LinkedHashMap<>();
        cinema.put("name", text(body.get("cinemaName")));
        cinema.put("brand", text(body.get("brand")));
        cinema.put("city", text(body.getOrDefault("city", "南京")));
        cinema.put("address", text(body.get("address")));
        cinema.put("phone", text(body.get("phone")));
        cinema.put("intro", text(body.get("intro")));
        cinema.put("facilities", text(body.get("facilities")));
        cinema.put("ownerId", uid);
        authMapper.insertCinema(cinema);
        return loginResult(authMapper.findById(uid));
    }

    public Map<String, Object> me(Long id) {
        Map<String, Object> user = authMapper.findById(id);
        if (user == null) throw new BizException("用户不存在", 404);
        return user;
    }

    public Map<String, Object> updateProfile(Long id, Map<String, Object> body) {
        authMapper.updateProfile(id, text(body.get("name")), text(body.get("phone")),
                text(body.get("email")), text(body.get("avatar")));
        return authMapper.findById(id);
    }

    public void changePassword(Long id, String current, String next) {
        Map<String, Object> user = authMapper.findByUsername(String.valueOf(authMapper.findById(id).get("username")));
        if (!BCrypt.checkpw(current, String.valueOf(user.get("password")))) {
            throw new BizException("当前密码不正确");
        }
        if (next == null || next.length() < 6) throw new BizException("新密码至少需要6位");
        authMapper.updatePassword(id, BCrypt.hashpw(next));
    }

    public Map<String, Object> loginResult(Map<String, Object> raw) {
        Map<String, Object> user = new LinkedHashMap<>(raw);
        user.remove("password");
        AuthContext.Principal principal = new AuthContext.Principal(
                ((Number) user.get("id")).longValue(),
                String.valueOf(user.get("username")),
                String.valueOf(user.get("name")),
                String.valueOf(user.get("role"))
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", jwtService.create(principal));
        result.put("user", user);
        return result;
    }

    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
}
