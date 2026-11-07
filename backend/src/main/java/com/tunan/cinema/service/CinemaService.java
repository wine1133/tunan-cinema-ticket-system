package com.tunan.cinema.service;

import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.CinemaMapper;
import com.tunan.cinema.mapper.PublicMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CinemaService {
    private final CinemaMapper cinemaMapper;
    private final PublicMapper publicMapper;

    public CinemaService(CinemaMapper cinemaMapper, PublicMapper publicMapper) {
        this.cinemaMapper = cinemaMapper;
        this.publicMapper = publicMapper;
    }

    public Map<String, Object> currentCinema(Long ownerId) {
        Map<String, Object> cinema = cinemaMapper.findByOwner(ownerId);
        if (cinema == null) throw new BizException("当前账号尚未关联影院", 404);
        return cinema;
    }

    @Transactional
    public Map<String, Object> saveShowing(Long ownerId, Map<String, Object> body, Long id) {
        Map<String, Object> cinema = currentCinema(ownerId);
        if (!"APPROVED".equals(cinema.get("status"))) throw new BizException("影院通过认证后才能提交排片");
        Long cinemaId = ((Number) cinema.get("id")).longValue();
        Long movieId = longValue(body.get("movieId"));
        Long roomId = longValue(body.get("roomId"));
        String start = text(body.get("startTime"));
        if (start.length() == 16) start += ":00";
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new BizException("请选择有效的放映时间");
        }
        if (startTime.isBefore(LocalDateTime.now().plusMinutes(30))) throw new BizException("放映时间需至少在30分钟之后");
        Map<String, Object> movie = publicMapper.findMovie(movieId);
        if (movie == null) throw new BizException("电影不存在");
        int duration = ((Number) movie.get("duration")).intValue();
        boolean roomOwned = cinemaMapper.rooms(cinemaId).stream()
                .anyMatch(r -> ((Number) r.get("id")).longValue() == roomId);
        if (!roomOwned) throw new BizException("影厅不属于当前影院");
        Map<String, Object> data = new LinkedHashMap<>(body);
        data.put("cinemaId", cinemaId);
        data.put("movieId", movieId);
        data.put("roomId", roomId);
        data.put("startTime", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("endTime", startTime.plusMinutes(duration).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("price", new BigDecimal(String.valueOf(body.get("price"))));
        data.put("language", text(body.getOrDefault("language", "国语")));
        data.put("version", text(body.getOrDefault("version", "2D")));
        if (id == null) {
            cinemaMapper.insertShowing(data);
            data.put("id", data.get("id"));
        } else {
            data.put("id", id);
            if (cinemaMapper.updateShowing(data) == 0) throw new BizException("仅待审批或已驳回场次可以修改");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", data.get("id"));
        result.put("message", "排片申请已提交，等待平台审批");
        return result;
    }

    private Long longValue(Object value) {
        if (value == null) throw new BizException("请选择电影和影厅");
        return Long.valueOf(String.valueOf(value));
    }
    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
}
