package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("""
        SELECT m.id, m.title, m.poster, m.score, m.status, m.release_date AS releaseDate,
               m.box_office AS boxOffice, m.want_count AS wantCount,
               DATE_FORMAT(f.created_at, '%Y-%m-%d') AS favoriteAt
        FROM favorites f JOIN movies m ON m.id=f.movie_id
        WHERE f.user_id=#{userId} ORDER BY f.created_at DESC
        """)
    List<Map<String, Object>> favorites(Long userId);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id=#{userId} AND movie_id=#{movieId}")
    int isFavorite(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Insert("INSERT IGNORE INTO favorites(user_id, movie_id) VALUES(#{userId}, #{movieId})")
    int addFavorite(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Delete("DELETE FROM favorites WHERE user_id=#{userId} AND movie_id=#{movieId}")
    int removeFavorite(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Insert("""
        INSERT INTO reviews(user_id, movie_id, score, content)
        VALUES(#{userId}, #{movieId}, #{score}, #{content})
        ON DUPLICATE KEY UPDATE score=VALUES(score), content=VALUES(content), created_at=NOW()
        """)
    int saveReview(@Param("userId") Long userId, @Param("movieId") Long movieId,
                   @Param("score") BigDecimal score, @Param("content") String content);

    @Select("SELECT score, content FROM reviews WHERE user_id=#{userId} AND movie_id=#{movieId}")
    Map<String, Object> myReview(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Update("""
        UPDATE movies SET score=(
          SELECT ROUND(AVG(score),1) FROM reviews WHERE movie_id=#{movieId}
        ) WHERE id=#{movieId} AND (SELECT COUNT(*) FROM reviews WHERE movie_id=#{movieId}) >= 3
        """)
    int refreshMovieScore(Long movieId);

    @Select("""
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               m.title AS movieTitle, m.poster, m.duration,
               c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName, r.seat_rows AS seatRows, r.seat_cols AS seatCols
        FROM showings s JOIN movies m ON m.id=s.movie_id
        JOIN cinemas c ON c.id=s.cinema_id JOIN rooms r ON r.id=s.room_id
        WHERE s.id=#{id}
        """)
    Map<String, Object> showingForOrder(Long id);

    @Select("""
        SELECT COUNT(*) FROM order_seats os JOIN orders o ON o.id=os.order_id
        WHERE os.showing_id=#{showingId} AND os.row_no=#{rowNo} AND os.col_no=#{colNo}
          AND (o.status IN ('PAID','USED') OR (o.status='UNPAID' AND o.created_at > DATE_SUB(NOW(), INTERVAL 10 MINUTE)))
        """)
    int seatTaken(@Param("showingId") Long showingId, @Param("rowNo") Integer rowNo, @Param("colNo") Integer colNo);

    @Insert("""
        INSERT INTO orders(order_no, user_id, showing_id, amount, status, pickup_code)
        VALUES(#{orderNo}, #{userId}, #{showingId}, #{amount}, 'UNPAID', #{pickupCode})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Map<String, Object> data);

    @Insert("INSERT INTO order_seats(order_id, showing_id, row_no, col_no, price) VALUES(#{orderId}, #{showingId}, #{rowNo}, #{colNo}, #{price})")
    int insertOrderSeat(@Param("orderId") Long orderId, @Param("showingId") Long showingId,
                        @Param("rowNo") Integer rowNo, @Param("colNo") Integer colNo,
                        @Param("price") BigDecimal price);

    @Update("UPDATE orders SET status='PAID', paid_at=NOW(), pay_method=#{method} WHERE id=#{id} AND user_id=#{userId} AND status='UNPAID'")
    int payOrder(@Param("id") Long id, @Param("userId") Long userId, @Param("method") String method);

    @Update("UPDATE orders SET status='CANCELLED' WHERE id=#{id} AND user_id=#{userId} AND status IN ('UNPAID','PAID')")
    int cancelOrder(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE orders SET status='USED' WHERE id=#{id} AND status='PAID'")
    int pickupOrder(Long id);

    @Select("""
        SELECT o.id, o.order_no AS orderNo, o.user_id AS userId, o.showing_id AS showingId,
               o.amount, o.status, o.pickup_code AS pickupCode, o.pay_method AS payMethod,
               DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               DATE_FORMAT(o.paid_at, '%Y-%m-%d %H:%i') AS paidAt,
               m.id AS movieId, m.title AS movieTitle, m.poster,
               c.id AS cinemaId, c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime
        FROM orders o JOIN showings s ON s.id=o.showing_id
        JOIN movies m ON m.id=s.movie_id JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        WHERE o.user_id=#{userId}
        ORDER BY o.created_at DESC
        """)
    List<Map<String, Object>> userOrders(Long userId);

    @Select("""
        SELECT o.id, o.order_no AS orderNo, o.user_id AS userId, o.showing_id AS showingId,
               o.amount, o.status, o.pickup_code AS pickupCode, o.pay_method AS payMethod,
               DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               DATE_FORMAT(o.paid_at, '%Y-%m-%d %H:%i') AS paidAt,
               m.id AS movieId, m.title AS movieTitle, m.poster,
               c.id AS cinemaId, c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName, DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime
        FROM orders o JOIN showings s ON s.id=o.showing_id
        JOIN movies m ON m.id=s.movie_id JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        WHERE o.id=#{orderId} AND o.user_id=#{userId}
        """)
    Map<String, Object> userOrderById(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Select("""
        SELECT o.* FROM orders o WHERE o.order_no=#{orderNo} AND o.user_id=#{userId}
        """)
    Map<String, Object> userOrderByNo(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Select("""
        SELECT row_no AS rowNo, col_no AS colNo, price FROM order_seats WHERE order_id=#{orderId} ORDER BY row_no, col_no
        """)
    List<Map<String, Object>> orderSeats(Long orderId);
}
