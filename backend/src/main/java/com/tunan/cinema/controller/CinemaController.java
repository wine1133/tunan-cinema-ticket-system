package com.tunan.cinema.controller;

import com.tunan.cinema.auth.AuthContext;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.common.RequireRole;
import com.tunan.cinema.mapper.CinemaMapper;
import com.tunan.cinema.service.CinemaService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RequireRole("CINEMA")
@RestController
@RequestMapping("/api/cinema")
public class CinemaController {
    private final CinemaService service;
    private final CinemaMapper mapper;

    public CinemaController(CinemaService service, CinemaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/info")
    public Map<String, Object> info() { return service.currentCinema(AuthContext.get().id()); }

    @PutMapping("/info")
    public Map<String, Object> updateInfo(@RequestBody Map<String, Object> body) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", cinema.get("id"));
        data.put("ownerId", AuthContext.get().id());
        mapper.updateCinema(data);
        return Map.of("message", "影院资料已更新，认证信息需重新审核");
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        long id = ((Number) cinema.get("id")).longValue();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cinema", cinema);
        data.put("roomCount", mapper.roomCount(id));
        data.put("showingCount", mapper.activeShowingCount(id));
        data.put("todayRevenue", mapper.todayRevenue(id));
        data.put("paidOrderCount", mapper.paidOrderCount(id));
        data.put("showings", mapper.showings(id).stream().limit(6).toList());
        data.put("orders", mapper.orders(id).stream().limit(5).toList());
        return data;
    }

    @GetMapping("/rooms")
    public Object rooms() {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        return mapper.rooms(((Number) cinema.get("id")).longValue());
    }

    @PostMapping("/rooms")
    public Map<String, Object> addRoom(@RequestBody Map<String, Object> body) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("cinemaId", ((Number) cinema.get("id")).longValue());
        data.putIfAbsent("seatRows", 8);
        data.putIfAbsent("seatCols", 8);
        data.putIfAbsent("roomType", "激光厅");
        mapper.insertRoom(data);
        return Map.of("id", data.get("id"), "message", "影厅创建成功");
    }

    @PutMapping("/rooms/{id}")
    public Map<String, Object> updateRoom(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("id", id);
        data.put("cinemaId", ((Number) cinema.get("id")).longValue());
        data.putIfAbsent("status", 1);
        if (mapper.updateRoom(data) == 0) throw new BizException("影厅不存在");
        return Map.of("message", "影厅信息已更新");
    }

    @DeleteMapping("/rooms/{id}")
    public Map<String, Object> deleteRoom(@PathVariable Long id) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        if (mapper.deleteRoom(id, ((Number) cinema.get("id")).longValue()) == 0) throw new BizException("影厅不存在或已关联排片");
        return Map.of("message", "影厅已删除");
    }

    @GetMapping("/showings")
    public Object showings() {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        return mapper.showings(((Number) cinema.get("id")).longValue());
    }

    @PostMapping("/showings")
    public Map<String, Object> addShowing(@RequestBody Map<String, Object> body) {
        return service.saveShowing(AuthContext.get().id(), body, null);
    }

    @PutMapping("/showings/{id}")
    public Map<String, Object> updateShowing(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.saveShowing(AuthContext.get().id(), body, id);
    }

    @DeleteMapping("/showings/{id}")
    public Map<String, Object> deleteShowing(@PathVariable Long id) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        if (mapper.deleteShowing(id, ((Number) cinema.get("id")).longValue()) == 0) throw new BizException("仅待审批或已驳回场次可以删除");
        return Map.of("message", "排片申请已删除");
    }

    @GetMapping("/movie-options")
    public Object movieOptions() { return mapper.movieOptions(); }

    @GetMapping("/orders")
    public Object orders() {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        return mapper.orders(((Number) cinema.get("id")).longValue());
    }

    @PostMapping("/orders/{id}/pickup")
    public Map<String, Object> pickup(@PathVariable Long id) {
        Map<String, Object> cinema = service.currentCinema(AuthContext.get().id());
        long cinemaId = ((Number) cinema.get("id")).longValue();
        if (mapper.orderBelongs(id, cinemaId) == 0) throw new BizException("订单不属于当前影院");
        if (mapper.pickupOrder(id) == 0) throw new BizException("仅已支付订单可以核销");
        return Map.of("message", "取票核销成功");
    }
}
