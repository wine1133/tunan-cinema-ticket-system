package com.tunan.cinema.service;

import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.PublicMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PublicService {
    private final PublicMapper mapper;

    public PublicService(PublicMapper mapper) { this.mapper = mapper; }

    public Map<String, Object> home() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("now", mapper.nowMovies(8));
        data.put("soon", mapper.soonMovies(6));
        data.put("boxOffice", mapper.boxOfficeMovies(10));
        data.put("scoreRank", mapper.scoreMovies(10));
        data.put("notices", mapper.notices(3));
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("todayBoxOffice", mapper.todayBoxOffice());
        stats.put("todayOrders", mapper.todayOrders());
        stats.put("userCount", mapper.userCount());
        stats.put("showingCount", mapper.showingCount());
        data.put("stats", stats);
        return data;
    }

    public Map<String, Object> moviePage(String status, Long genreId, String keyword, int page, int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", mapper.listMovies(status, keyword, genreId, page, size));
        data.put("total", mapper.countMovies(status, keyword, genreId));
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    public Map<String, Object> movieDetail(Long id) {
        Map<String, Object> movie = mapper.findMovie(id);
        if (movie == null) throw new BizException("电影不存在", 404);
        movie.put("genres", mapper.movieGenres(id));
        movie.put("cast", mapper.movieCast(id));
        movie.put("reviews", mapper.movieReviews(id));
        movie.put("favoriteCount", mapper.favoriteCount(id));
        movie.put("showings", mapper.movieShowings(id));
        return movie;
    }

    public Map<String, Object> seatMap(Long showingId) {
        Map<String, Object> showing = mapper.findShowing(showingId);
        if (showing == null) throw new BizException("场次不存在", 404);
        int rows = ((Number) showing.get("seatRows")).intValue();
        int cols = ((Number) showing.get("seatCols")).intValue();
        Set<String> occupied = new HashSet<>();
        for (Map<String, Object> seat : mapper.occupiedSeats(showingId)) {
            occupied.add(seat.get("rowNo") + "-" + seat.get("colNo"));
        }
        List<Map<String, Object>> seats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Map<String, Object> seat = new LinkedHashMap<>();
                seat.put("row", r);
                seat.put("col", c);
                seat.put("label", r + "排" + c + "座");
                seat.put("occupied", occupied.contains(r + "-" + c));
                seat.put("aisleAfter", c == 2 || c == cols - 2);
                seats.add(seat);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("showing", showing);
        data.put("rows", rows);
        data.put("cols", cols);
        data.put("seats", seats);
        return data;
    }

    public Map<String, Object> rankings(String type) {
        List<Map<String, Object>> rows;
        if ("score".equalsIgnoreCase(type)) rows = mapper.scoreMovies(50);
        else if ("want".equalsIgnoreCase(type)) rows = mapper.wantMovies(50);
        else rows = mapper.boxOfficeMovies(50);
        for (int i = 0; i < rows.size(); i++) rows.get(i).put("rank", i + 1);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", type == null ? "boxoffice" : type);
        data.put("records", rows);
        return data;
    }

    public Map<String, Object> cinemaDetail(Long id) {
        Map<String, Object> cinema = mapper.findCinema(id);
        if (cinema == null) throw new BizException("影院不存在", 404);
        String today = LocalDate.now().toString();
        cinema.put("showings", mapper.cinemaShowings(id, today));
        cinema.put("allShowings", mapper.cinemaShowings(id, null));
        return cinema;
    }

    public Map<String, Object> liveStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("todayBoxOffice", mapper.todayBoxOffice());
        stats.put("todayOrders", mapper.todayOrders());
        stats.put("userCount", mapper.userCount());
        stats.put("showingCount", mapper.showingCount());
        stats.put("time", new Date());
        return stats;
    }
}
