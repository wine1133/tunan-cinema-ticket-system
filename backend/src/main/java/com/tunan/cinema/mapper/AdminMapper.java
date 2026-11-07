package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    @Select("SELECT COUNT(*) FROM movies")
    int movieCount();

    @Select("SELECT COUNT(*) FROM cinemas WHERE status='APPROVED'")
    int cinemaCount();

    @Select("SELECT COUNT(*) FROM users WHERE role='USER'")
    int userCount();

    @Select("SELECT COUNT(*) FROM showings WHERE approval_status='PENDING'")
    int pendingShowings();

    @Select("SELECT COUNT(*) FROM cinemas WHERE status='PENDING'")
    int pendingCinemas();

    @Select("""
        SELECT COALESCE(SUM(amount),0) FROM orders
        WHERE status IN ('PAID','USED') AND DATE(created_at)=CURDATE()
        """)
    java.math.BigDecimal todayRevenue();

    @Select("""
        SELECT DATE_FORMAT(created_at, '%m-%d') AS day, COALESCE(SUM(amount),0) AS amount
        FROM orders WHERE status IN ('PAID','USED') AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
        GROUP BY DATE_FORMAT(created_at, '%m-%d') ORDER BY DATE_FORMAT(created_at, '%m-%d')
        """)
    List<Map<String, Object>> salesTrend();

    @Select("""
        SELECT g.name, COUNT(DISTINCT m.id) AS value
        FROM genres g JOIN movie_genres mg ON mg.genre_id=g.id JOIN movies m ON m.id=mg.movie_id
        GROUP BY g.id, g.name ORDER BY value DESC
        """)
    List<Map<String, Object>> genreDistribution();

    @Select("""
        SELECT g.name, COALESCE(SUM(m.box_office),0) AS amount
        FROM genres g JOIN movie_genres mg ON mg.genre_id=g.id JOIN movies m ON m.id=mg.movie_id
        GROUP BY g.id, g.name ORDER BY amount DESC LIMIT 8
        """)
    List<Map<String, Object>> genreBoxOffice();

    @Select("""
        SELECT m.id, m.title, m.english_title AS englishTitle, m.director, m.duration,
               m.release_date AS releaseDate, m.status, m.score, m.box_office AS boxOffice,
               m.want_count AS wantCount, m.poster, m.backdrop, m.trailer_url AS trailerUrl,
               m.synopsis, m.is_featured AS isFeatured,
               (SELECT GROUP_CONCAT(g.name ORDER BY g.sort_no SEPARATOR ',')
                  FROM movie_genres mg JOIN genres g ON g.id=mg.genre_id
                 WHERE mg.movie_id=m.id) AS genreNames
        FROM movies m ORDER BY m.id DESC
        """)
    List<Map<String, Object>> movies();

    @Insert("""
        INSERT INTO movies(title, english_title, director, duration, release_date, status, score,
                           box_office, want_count, poster, backdrop, trailer_url, synopsis, is_featured)
        VALUES(#{title}, #{englishTitle}, #{director}, #{duration}, #{releaseDate}, #{status},
               #{score}, #{boxOffice}, #{wantCount}, #{poster}, #{backdrop}, #{trailerUrl}, #{synopsis}, #{isFeatured})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMovie(Map<String, Object> data);

    @Update("""
        UPDATE movies SET title=#{title}, english_title=#{englishTitle}, director=#{director},
               duration=#{duration}, release_date=#{releaseDate}, status=#{status}, score=#{score},
               box_office=#{boxOffice}, want_count=#{wantCount}, poster=#{poster}, backdrop=#{backdrop},
               trailer_url=#{trailerUrl}, synopsis=#{synopsis}, is_featured=#{isFeatured}
        WHERE id=#{id}
        """)
    int updateMovie(Map<String, Object> data);

    @Delete("DELETE FROM movies WHERE id=#{id}")
    int deleteMovie(Long id);

    @Delete("DELETE FROM movie_genres WHERE movie_id=#{movieId}")
    int clearMovieGenres(Long movieId);

    @Insert("INSERT INTO movie_genres(movie_id, genre_id) VALUES(#{movieId}, #{genreId})")
    int addMovieGenre(@Param("movieId") Long movieId, @Param("genreId") Long genreId);

    @Select("""
        SELECT id, name, role_name AS roleName, sort_no AS sortNo
        FROM movie_cast WHERE movie_id=#{movieId} ORDER BY sort_no
        """)
    List<Map<String, Object>> cast(Long movieId);

    @Insert("""
        INSERT INTO movie_cast(movie_id, name, role_name, sort_no)
        VALUES(#{movieId}, #{name}, #{roleName}, #{sortNo})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCast(Map<String, Object> data);

    @Delete("DELETE FROM movie_cast WHERE id=#{id}")
    int deleteCast(Long id);

    @Select("""
        SELECT c.id, c.name, c.brand, c.city, c.address, c.phone, c.cover, c.intro, c.facilities,
               c.status, c.owner_id AS ownerId, u.name AS ownerName,
               DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               DATE_FORMAT(c.audit_time, '%Y-%m-%d %H:%i') AS auditTime, c.audit_remark AS auditRemark,
               (SELECT COUNT(*) FROM rooms r WHERE r.cinema_id=c.id) AS roomCount
        FROM cinemas c LEFT JOIN users u ON u.id=c.owner_id ORDER BY c.status='PENDING' DESC, c.id DESC
        """)
    List<Map<String, Object>> cinemas();

    @Update("""
        UPDATE cinemas SET status=#{status}, audit_remark=#{remark}, audit_time=NOW() WHERE id=#{id}
        """)
    int auditCinema(@Param("id") Long id, @Param("status") String status, @Param("remark") String remark);

    @Update("""
        UPDATE cinemas SET name=#{name}, brand=#{brand}, city=#{city}, address=#{address},
               phone=#{phone}, intro=#{intro}, facilities=#{facilities}, status=#{status}
        WHERE id=#{id}
        """)
    int updateCinema(Map<String, Object> data);

    @Select("""
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               s.audit_remark AS auditRemark, DATE_FORMAT(s.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               m.title AS movieTitle, m.poster, c.name AS cinemaName, r.name AS roomName
        FROM showings s JOIN movies m ON m.id=s.movie_id JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        ORDER BY s.approval_status='PENDING' DESC, s.start_time DESC
        """)
    List<Map<String, Object>> showings();

    @Update("""
        UPDATE showings SET approval_status=#{status}, audit_remark=#{remark} WHERE id=#{id}
        """)
    int auditShowing(@Param("id") Long id, @Param("status") String status, @Param("remark") String remark);

    @Select("""
        SELECT o.id, o.order_no AS orderNo, o.amount, o.status, o.pickup_code AS pickupCode,
               DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               u.name AS userName, u.phone AS userPhone,
               m.title AS movieTitle, c.name AS cinemaName,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime, r.name AS roomName,
               (SELECT GROUP_CONCAT(CONCAT(os.row_no, '排', os.col_no, '座') ORDER BY os.row_no, os.col_no)
                  FROM order_seats os WHERE os.order_id=o.id) AS seatNames
        FROM orders o JOIN users u ON u.id=o.user_id JOIN showings s ON s.id=o.showing_id
        JOIN movies m ON m.id=s.movie_id JOIN cinemas c ON c.id=s.cinema_id JOIN rooms r ON r.id=s.room_id
        ORDER BY o.created_at DESC
        """)
    List<Map<String, Object>> orders();

    @Select("""
        SELECT id, username, name, phone, email, role, status,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM users ORDER BY role, id DESC
        """)
    List<Map<String, Object>> users();

    @Select("""
        SELECT r.id, r.cinema_id AS cinemaId, r.name, r.seat_rows AS seatRows, r.seat_cols AS seatCols,
               r.room_type AS roomType, r.status, DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i') AS createdAt,
               c.name AS cinemaName
        FROM rooms r JOIN cinemas c ON c.id=r.cinema_id ORDER BY c.id, r.id
        """)
    List<Map<String, Object>> rooms();

    @Insert("""
        INSERT INTO rooms(cinema_id, name, seat_rows, seat_cols, room_type, status)
        VALUES(#{cinemaId}, #{name}, #{seatRows}, #{seatCols}, #{roomType}, #{status})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRoom(Map<String, Object> data);

    @Update("""
        UPDATE rooms SET cinema_id=#{cinemaId}, name=#{name}, seat_rows=#{seatRows}, seat_cols=#{seatCols},
               room_type=#{roomType}, status=#{status} WHERE id=#{id}
        """)
    int updateRoom(Map<String, Object> data);

    @Delete("DELETE FROM rooms WHERE id=#{id}")
    int deleteRoom(Long id);

    @Select("SELECT id, name, color, sort_no AS sortNo FROM genres ORDER BY sort_no, id")
    List<Map<String, Object>> genres();

    @Insert("INSERT INTO genres(name, color, sort_no) VALUES(#{name}, #{color}, #{sortNo})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGenre(Map<String, Object> data);

    @Update("UPDATE genres SET name=#{name}, color=#{color}, sort_no=#{sortNo} WHERE id=#{id}")
    int updateGenre(Map<String, Object> data);

    @Delete("DELETE FROM genres WHERE id=#{id}")
    int deleteGenre(Long id);

    @Select("""
        SELECT id, title, content, type, status,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM notices ORDER BY id DESC
        """)
    List<Map<String, Object>> notices();

    @Insert("INSERT INTO notices(title, content, type, status) VALUES(#{title}, #{content}, #{type}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertNotice(Map<String, Object> data);

    @Update("UPDATE notices SET title=#{title}, content=#{content}, type=#{type}, status=#{status} WHERE id=#{id}")
    int updateNotice(Map<String, Object> data);

    @Delete("DELETE FROM notices WHERE id=#{id}")
    int deleteNotice(Long id);
}
