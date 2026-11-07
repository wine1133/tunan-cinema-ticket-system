package com.tunan.cinema.service;

import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.PublicMapper;
import com.tunan.cinema.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PublicMapper publicMapper;
    private final Random random = new Random();

    public UserService(UserMapper userMapper, PublicMapper publicMapper) {
        this.userMapper = userMapper;
        this.publicMapper = publicMapper;
    }

    @Transactional
    public Map<String, Object> createOrder(Long userId, Long showingId, List<Map<String, Object>> seats) {
        if (seats == null || seats.isEmpty()) throw new BizException("请至少选择一个座位");
        if (seats.size() > 6) throw new BizException("单次最多选择6个座位");
        Map<String, Object> showing = userMapper.showingForOrder(showingId);
        if (showing == null || !"APPROVED".equals(showing.get("approvalStatus"))) {
            throw new BizException("该场次暂不可购票");
        }
        int rows = ((Number) showing.get("seatRows")).intValue();
        int cols = ((Number) showing.get("seatCols")).intValue();
        BigDecimal price = new BigDecimal(String.valueOf(showing.get("price")));
        Set<String> unique = new HashSet<>();
        for (Map<String, Object> seat : seats) {
            int row = ((Number) seat.get("row")).intValue();
            int col = ((Number) seat.get("col")).intValue();
            if (row < 1 || row > rows || col < 1 || col > cols) throw new BizException("座位号超出影厅范围");
            if (!unique.add(row + "-" + col)) throw new BizException("请勿重复选择同一座位");
            if (userMapper.seatTaken(showingId, row, col) > 0) throw new BizException(row + "排" + col + "座已被购买，请重新选择");
        }
        String orderNo = "TN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", random.nextInt(10000));
        String pickupCode = String.format("%06d", random.nextInt(1000000));
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderNo", orderNo);
        order.put("userId", userId);
        order.put("showingId", showingId);
        order.put("amount", price.multiply(BigDecimal.valueOf(seats.size())));
        order.put("pickupCode", pickupCode);
        userMapper.insertOrder(order);
        long orderId = ((Number) order.get("id")).longValue();
        for (Map<String, Object> seat : seats) {
            userMapper.insertOrderSeat(orderId, showingId, ((Number) seat.get("row")).intValue(),
                    ((Number) seat.get("col")).intValue(), price);
        }
        return orderDetail(userId, orderId);
    }

    public Map<String, Object> orderDetail(Long userId, Long orderId) {
        Map<String, Object> order = userMapper.userOrderById(orderId, userId);
        if (order == null) throw new BizException("订单不存在", 404);
        order.put("seats", userMapper.orderSeats(orderId));
        return order;
    }

    public Map<String, Object> pay(Long userId, Long orderId, String method) {
        if (userMapper.payOrder(orderId, userId, method == null ? "微信支付" : method) == 0) {
            throw new BizException("订单状态已变化，无法支付");
        }
        return orderDetail(userId, orderId);
    }

    public Map<String, Object> cancel(Long userId, Long orderId) {
        if (userMapper.cancelOrder(orderId, userId) == 0) {
            throw new BizException("仅待支付或未取票订单可以取消");
        }
        return orderDetail(userId, orderId);
    }

    public Map<String, Object> track(Long userId, String orderNo) {
        Map<String, Object> order = userMapper.userOrderByNo(orderNo, userId);
        if (order == null) throw new BizException("未找到该订单", 404);
        long id = ((Number) order.get("id")).longValue();
        return orderDetail(userId, id);
    }

    public Map<String, Object> toggleFavorite(Long userId, Long movieId, boolean favorite) {
        if (favorite) userMapper.addFavorite(userId, movieId);
        else userMapper.removeFavorite(userId, movieId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("favorite", favorite);
        result.put("count", publicMapper.favoriteCount(movieId));
        return result;
    }

    public Map<String, Object> review(Long userId, Long movieId, BigDecimal score, String content) {
        if (score == null || score.doubleValue() < 0 || score.doubleValue() > 10) {
            throw new BizException("评分需在0到10之间");
        }
        userMapper.saveReview(userId, movieId, score, content == null ? "" : content.trim());
        userMapper.refreshMovieScore(movieId);
        return publicMapper.findMovie(movieId);
    }
}
