package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CinemaMapper {
    @Select("""
        SELECT id, name, brand, city, address, phone, cover, intro, facilities, status,
               owner_id AS ownerId, audit_remark AS auditRemark,
               DATE_FORMAT(audit_time, '%Y-%m-%d %H:%i') AS auditTime
        FROM cinemas WHERE owner_id=#{ownerId} ORDER BY id DESC LIMIT 1
        """)
    Map<String, Object> findByOwner(Long ownerId);

    @Select("SELECT COUNT(*) FROM rooms WHERE cinema_id=#{cinemaId} AND status=1")
    int roomCount(Long cinemaId);

    @Select("SELECT COUNT(*) FROM showings WHERE cinema_id=#{cinemaId} AND approval_status='APPROVED' AND start_time>NOW()")
    int activeShowingCount(Long cinemaId);

    @Select("""
        SELECT COALESCE(SUM(o.amount),0) FROM orders o JOIN showings s ON s.id=o.showing_id
        WHERE s.cinema_id=#{cinemaId} AND o.status IN ('PAID','USED') AND DATE(o.created_at)=CURDATE()
        """)
    java.math.BigDecimal todayRevenue(Long cinemaId);

    @Select("""
        SELECT COUNT(*) FROM orders o JOIN showings s ON s.id=o.showing_id
        WHERE s.cinema_id=#{cinemaId} AND o.status='PAID'
        """)
    int paidOrderCount(Long cinemaId);

    @Select("""
        SELECT r.id, r.cinema_id AS cinemaId, r.name, r.seat_rows AS seatRows,
               r.seat_cols AS seatCols, r.room_type AS roomType, r.status,
               DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM rooms r WHERE r.cinema_id=#{cinemaId} ORDER BY r.id
        """)
    List<Map<String, Object>> rooms(Long cinemaId);

    @Insert("""
        INSERT INTO rooms(cinema_id, name, seat_rows, seat_cols, room_type, status)
        VALUES(#{cinemaId}, #{name}, #{seatRows}, #{seatCols}, #{roomType}, 1)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRoom(Map<String, Object> data);

    @Update("""
        UPDATE rooms SET name=#{name}, seat_rows=#{seatRows}, seat_cols=#{seatCols}, room_type=#{roomType}, status=#{status}
        WHERE id=#{id} AND cinema_id=#{cinemaId}
        """)
    int updateRoom(Map<String, Object> data);

    @Delete("DELETE FROM rooms WHERE id=#{roomId} AND cinema_id=#{cinemaId}")
    int deleteRoom(@Param("roomId") Long roomId, @Param("cinemaId") Long cinemaId);

    @Select("""
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               s.audit_remark AS auditRemark,
               DATE_FORMAT(s.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               m.title AS movieTitle, m.poster, r.name AS roomName,
               (SELECT COUNT(*) FROM order_seats os JOIN orders o ON o.id=os.order_id
                 WHERE os.showing_id=s.id AND o.status IN ('PAID','USED')) AS soldCount
        FROM showings s JOIN movies m ON m.id=s.movie_id JOIN rooms r ON r.id=s.room_id
        WHERE s.cinema_id=#{cinemaId} ORDER BY s.start_time DESC
        """)
    List<Map<String, Object>> showings(Long cinemaId);

    @Insert("""
        INSERT INTO showings(movie_id, cinema_id, room_id, start_time, end_time, price, language, version, approval_status)
        VALUES(#{movieId}, #{cinemaId}, #{roomId}, #{startTime}, #{endTime}, #{price}, #{language}, #{version}, 'PENDING')
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertShowing(Map<String, Object> data);

    @Update("""
        UPDATE showings SET movie_id=#{movieId}, room_id=#{roomId}, start_time=#{startTime},
               end_time=#{endTime}, price=#{price}, language=#{language}, version=#{version},
               approval_status='PENDING', audit_remark=NULL
        WHERE id=#{id} AND cinema_id=#{cinemaId} AND approval_status IN ('PENDING','REJECTED')
        """)
    int updateShowing(Map<String, Object> data);

    @Delete("DELETE FROM showings WHERE id=#{showingId} AND cinema_id=#{cinemaId} AND approval_status IN ('PENDING','REJECTED')")
    int deleteShowing(@Param("showingId") Long showingId, @Param("cinemaId") Long cinemaId);

    @Select("""
        SELECT o.id, o.order_no AS orderNo, o.amount, o.status, o.pickup_code AS pickupCode,
               DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               u.name AS userName, u.phone AS userPhone,
               m.title AS movieTitle, DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               r.name AS roomName,
               (SELECT GROUP_CONCAT(CONCAT(os.row_no, '排', os.col_no, '座') ORDER BY os.row_no, os.col_no)
                  FROM order_seats os WHERE os.order_id=o.id) AS seatNames
        FROM orders o JOIN showings s ON s.id=o.showing_id
        JOIN users u ON u.id=o.user_id JOIN movies m ON m.id=s.movie_id JOIN rooms r ON r.id=s.room_id
        WHERE s.cinema_id=#{cinemaId}
        ORDER BY o.created_at DESC
        """)
    List<Map<String, Object>> orders(Long cinemaId);

    @Select("""
        SELECT COUNT(*) FROM orders o JOIN showings s ON s.id=o.showing_id
        WHERE o.id=#{orderId} AND s.cinema_id=#{cinemaId}
        """)
    int orderBelongs(@Param("orderId") Long orderId, @Param("cinemaId") Long cinemaId);

    @Update("UPDATE orders SET status='USED' WHERE id=#{orderId} AND status='PAID'")
    int pickupOrder(Long orderId);

    @Update("""
        UPDATE cinemas SET name=#{name}, brand=#{brand}, city=#{city}, address=#{address},
               phone=#{phone}, intro=#{intro}, facilities=#{facilities}, status='PENDING',
               audit_remark=NULL, audit_time=NULL
        WHERE id=#{id} AND owner_id=#{ownerId}
        """)
    int updateCinema(Map<String, Object> data);

    @Select("SELECT id, title, poster, status FROM movies ORDER BY status='NOW' DESC, release_date DESC")
    List<Map<String, Object>> movieOptions();
}
