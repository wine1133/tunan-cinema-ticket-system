package com.tunan.cinema.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.tunan.cinema.auth.AuthContext;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.common.RequireRole;
import com.tunan.cinema.mapper.AdminMapper;
import com.tunan.cinema.mapper.AuthMapper;
import com.tunan.cinema.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RequireRole("ADMIN")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminMapper mapper;
    private final AuthMapper authMapper;
    private final AdminService service;

    public AdminController(AdminMapper mapper, AuthMapper authMapper, AdminService service) {
        this.mapper = mapper;
        this.authMapper = authMapper;
        this.service = service;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() { return service.dashboard(); }

    @GetMapping("/movies")
    public Object movies() { return mapper.movies(); }

    @PostMapping("/movies")
    public Map<String, Object> addMovie(@RequestBody Map<String, Object> body) {
        validateGenres(body.get("genreIds"));
        Map<String, Object> data = movieData(body);
        mapper.insertMovie(data);
        saveGenres(((Number) data.get("id")).longValue(), body.get("genreIds"));
        return Map.of("id", data.get("id"), "message", "电影创建成功");
    }

    @PutMapping("/movies/{id}")
    public Map<String, Object> updateMovie(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        validateGenres(body.get("genreIds"));
        Map<String, Object> data = movieData(body);
        data.put("id", id);
        if (mapper.updateMovie(data) == 0) throw new BizException("电影不存在");
        saveGenres(id, body.get("genreIds"));
        return Map.of("message", "电影信息已更新");
    }

    @GetMapping("/rooms")
    public Object rooms() { return mapper.rooms(); }

    @PostMapping("/rooms")
    public Map<String, Object> addRoom(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        if (data.get("cinemaId") == null || String.valueOf(data.get("name")).isBlank()) throw new BizException("请选择影院并填写影厅名称");
        data.putIfAbsent("seatRows", 8);
        data.putIfAbsent("seatCols", 8);
        data.putIfAbsent("roomType", "激光厅");
        data.putIfAbsent("status", 1);
        mapper.insertRoom(data);
        return Map.of("id", data.get("id"), "message", "影厅已创建");
    }

    @PutMapping("/rooms/{id}")
    public Map<String, Object> updateRoom(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", id);
        data.putIfAbsent("status", 1);
        if (mapper.updateRoom(data) == 0) throw new BizException("影厅不存在");
        return Map.of("message", "影厅信息已更新");
    }

    @DeleteMapping("/rooms/{id}")
    public Map<String, Object> deleteRoom(@PathVariable Long id) {
        mapper.deleteRoom(id);
        return Map.of("message", "影厅已删除");
    }

    @DeleteMapping("/movies/{id}")
    public Map<String, Object> deleteMovie(@PathVariable Long id) {
        mapper.deleteMovie(id);
        return Map.of("message", "电影已删除");
    }

    @GetMapping("/movies/{id}/cast")
    public Object cast(@PathVariable Long id) { return mapper.cast(id); }

    @PostMapping("/movies/{id}/cast")
    public Map<String, Object> addCast(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("movieId", id);
        data.putIfAbsent("sortNo", 99);
        mapper.insertCast(data);
        return Map.of("id", data.get("id"), "message", "演职人员已添加");
    }

    @DeleteMapping("/cast/{id}")
    public Map<String, Object> deleteCast(@PathVariable Long id) {
        mapper.deleteCast(id);
        return Map.of("message", "演职人员已删除");
    }

    @GetMapping("/genres")
    public Object genres() { return mapper.genres(); }

    @PostMapping("/genres")
    public Map<String, Object> addGenre(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.putIfAbsent("color", "#ff6b4a");
        data.putIfAbsent("sortNo", 99);
        mapper.insertGenre(data);
        return Map.of("id", data.get("id"), "message", "类型已创建");
    }

    @PutMapping("/genres/{id}")
    public Map<String, Object> updateGenre(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", id);
        mapper.updateGenre(data);
        return Map.of("message", "类型已更新");
    }

    @DeleteMapping("/genres/{id}")
    public Map<String, Object> deleteGenre(@PathVariable Long id) {
        mapper.deleteGenre(id);
        return Map.of("message", "类型已删除");
    }

    @GetMapping("/cinemas")
    public Object cinemas() { return mapper.cinemas(); }

    @PostMapping("/cinemas/{id}/audit")
    public Map<String, Object> auditCinema(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = String.valueOf(body.getOrDefault("status", "APPROVED"));
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) throw new BizException("审核状态不合法");
        mapper.auditCinema(id, status, String.valueOf(body.getOrDefault("remark", "")));
        return Map.of("message", status.equals("APPROVED") ? "影院认证已通过" : "影院认证已驳回");
    }

    @PutMapping("/cinemas/{id}")
    public Map<String, Object> updateCinema(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", id);
        mapper.updateCinema(data);
        return Map.of("message", "影院信息已更新");
    }

    @GetMapping("/showings")
    public Object showings() { return mapper.showings(); }

    @PostMapping("/showings/{id}/audit")
    public Map<String, Object> auditShowing(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = String.valueOf(body.getOrDefault("status", "APPROVED"));
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) throw new BizException("审核状态不合法");
        mapper.auditShowing(id, status, String.valueOf(body.getOrDefault("remark", "")));
        return Map.of("message", status.equals("APPROVED") ? "排片申请已通过" : "排片申请已驳回");
    }

    @GetMapping("/orders")
    public Object orders() { return mapper.orders(); }

    @GetMapping("/users")
    public Object users() { return mapper.users(); }

    @PutMapping("/users/{id}/status")
    public Map<String, Object> userStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (id.equals(AuthContext.get().id())) throw new BizException("不能停用当前登录账号");
        authMapper.updateStatus(id, Integer.valueOf(String.valueOf(body.get("status"))));
        return Map.of("message", "账号状态已更新");
    }

    @GetMapping("/admins")
    public Object admins() { return authMapper.listByRole("ADMIN"); }

    @PostMapping("/admins")
    public Map<String, Object> addAdmin(@RequestBody Map<String, Object> body) {
        String username = String.valueOf(body.get("username"));
        String password = String.valueOf(body.get("password"));
        if (username.length() < 3 || password.length() < 6) throw new BizException("账号至少3位，密码至少6位");
        if (authMapper.countUsername(username) > 0) throw new BizException("账号已存在");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("password", BCrypt.hashpw(password));
        data.put("name", String.valueOf(body.getOrDefault("name", username)));
        data.put("phone", String.valueOf(body.getOrDefault("phone", "")));
        data.put("email", String.valueOf(body.getOrDefault("email", "")));
        data.put("role", "ADMIN");
        authMapper.insertUser(data);
        return Map.of("id", data.get("id"), "message", "管理员创建成功");
    }

    @GetMapping("/notices")
    public Object notices() { return mapper.notices(); }

    @PostMapping("/notices")
    public Map<String, Object> addNotice(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.putIfAbsent("status", 1);
        data.putIfAbsent("type", "系统公告");
        mapper.insertNotice(data);
        return Map.of("id", data.get("id"), "message", "公告已发布");
    }

    @PutMapping("/notices/{id}")
    public Map<String, Object> updateNotice(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", id);
        mapper.updateNotice(data);
        return Map.of("message", "公告已更新");
    }

    @DeleteMapping("/notices/{id}")
    public Map<String, Object> deleteNotice(@PathVariable Long id) {
        mapper.deleteNotice(id);
        return Map.of("message", "公告已删除");
    }

    private Map<String, Object> movieData(Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", String.valueOf(body.getOrDefault("title", "")));
        data.put("englishTitle", String.valueOf(body.getOrDefault("englishTitle", "")));
        data.put("director", String.valueOf(body.getOrDefault("director", "")));
        data.put("duration", Integer.valueOf(String.valueOf(body.getOrDefault("duration", 120))));
        data.put("releaseDate", body.get("releaseDate"));
        data.put("status", String.valueOf(body.getOrDefault("status", "NOW")));
        data.put("score", body.getOrDefault("score", 0));
        data.put("boxOffice", body.getOrDefault("boxOffice", 0));
        data.put("wantCount", body.getOrDefault("wantCount", 0));
        data.put("poster", String.valueOf(body.getOrDefault("poster", "/posters/star-echo.svg")));
        data.put("backdrop", String.valueOf(body.getOrDefault("backdrop", "/posters/star-echo-wide.svg")));
        data.put("trailerUrl", body.get("trailerUrl"));
        data.put("synopsis", String.valueOf(body.getOrDefault("synopsis", "")));
        data.put("isFeatured", Integer.valueOf(String.valueOf(body.getOrDefault("isFeatured", 0))));
        if (String.valueOf(data.get("title")).isBlank()) throw new BizException("请填写电影名称");
        return data;
    }

    private void validateGenres(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) throw new BizException("请至少选择一个电影类型");
        if (list.size() > 3) throw new BizException("一部电影最多只能选择3个类型");
    }

    private void saveGenres(Long movieId, Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) return;
        mapper.clearMovieGenres(movieId);
        for (Object id : list) mapper.addMovieGenre(movieId, Long.valueOf(String.valueOf(id)));
    }
}
