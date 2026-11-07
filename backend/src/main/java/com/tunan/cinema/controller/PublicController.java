package com.tunan.cinema.controller;

import com.tunan.cinema.common.PublicApi;
import com.tunan.cinema.mapper.PublicMapper;
import com.tunan.cinema.service.PublicService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@PublicApi
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final PublicService service;
    private final PublicMapper mapper;

    public PublicController(PublicService service, PublicMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/health")
    public Map<String, Object> health() { return Map.of("status", "UP", "name", "兔南电影购票系统"); }

    @GetMapping("/home")
    public Map<String, Object> home() { return service.home(); }

    @GetMapping("/movies")
    public Map<String, Object> movies(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long genreId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        return service.moviePage(status, genreId, keyword, page, size);
    }

    @GetMapping("/movies/{id}")
    public Map<String, Object> movie(@PathVariable Long id) { return service.movieDetail(id); }

    @GetMapping("/genres")
    public Object genres() { return mapper.genres(); }

    @GetMapping("/cinemas")
    public Object cinemas(@RequestParam(required = false) String keyword) { return mapper.cinemas(keyword); }

    @GetMapping("/cinemas/{id}")
    public Map<String, Object> cinema(@PathVariable Long id) { return service.cinemaDetail(id); }

    @GetMapping("/showings/{id}/seats")
    public Map<String, Object> seats(@PathVariable Long id) { return service.seatMap(id); }

    @GetMapping("/rankings")
    public Map<String, Object> rankings(@RequestParam(defaultValue = "boxoffice") String type) {
        return service.rankings(type);
    }

    @GetMapping("/notices")
    public Object notices(@RequestParam(defaultValue = "20") int limit) { return mapper.notices(Math.min(limit, 50)); }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return service.liveStats(); }

    @GetMapping("/meta")
    public Map<String, Object> meta() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("genres", mapper.genres());
        data.put("cities", java.util.List.of("南京"));
        return data;
    }
}
