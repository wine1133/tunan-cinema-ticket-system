package com.tunan.cinema.service;

import com.tunan.cinema.mapper.AdminMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminService {
    private final AdminMapper mapper;

    public AdminService(AdminMapper mapper) { this.mapper = mapper; }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("movies", mapper.movieCount());
        stats.put("cinemas", mapper.cinemaCount());
        stats.put("users", mapper.userCount());
        stats.put("pendingShowings", mapper.pendingShowings());
        stats.put("pendingCinemas", mapper.pendingCinemas());
        stats.put("todayRevenue", mapper.todayRevenue());
        data.put("stats", stats);

        Map<String, Object> trendMap = new HashMap<>();
        for (Map<String, Object> row : mapper.salesTrend()) {
            trendMap.put(String.valueOf(row.get("day")), row.get("amount"));
        }
        List<String> days = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            String label = day.format(DateTimeFormatter.ofPattern("MM-dd"));
            days.add(label);
            values.add(trendMap.getOrDefault(label, 0));
        }
        data.put("days", days);
        data.put("sales", values);
        data.put("genreDistribution", mapper.genreDistribution());
        data.put("genreBoxOffice", mapper.genreBoxOffice());
        return data;
    }
}
