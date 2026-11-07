package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PublicMapper {
    String MOVIE_SELECT = """
        SELECT m.id, m.title, m.english_title AS englishTitle, m.director, m.duration,
               m.release_date AS releaseDate, m.status, m.score, m.box_office AS boxOffice,
               m.want_count AS wantCount, m.poster, m.backdrop, m.trailer_url AS trailerUrl,
               m.synopsis, m.is_featured AS isFeatured,
               (SELECT GROUP_CONCAT(g.name ORDER BY g.sort_no SEPARATOR ',')
                  FROM movie_genres mg JOIN genres g ON g.id=mg.genre_id
                 WHERE mg.movie_id=m.id) AS genreNames
        FROM movies m
        """;

    @Select(MOVIE_SELECT + " WHERE m.status='NOW' ORDER BY m.is_featured DESC, m.score DESC, m.box_office DESC LIMIT #{limit}")
    List<Map<String, Object>> nowMovies(int limit);

    @Select(MOVIE_SELECT + " WHERE m.status='SOON' ORDER BY m.want_count DESC LIMIT #{limit}")
    List<Map<String, Object>> soonMovies(int limit);

    @Select(MOVIE_SELECT + " WHERE m.status='NOW' ORDER BY m.box_office DESC LIMIT #{limit}")
    List<Map<String, Object>> boxOfficeMovies(int limit);

    @Select(MOVIE_SELECT + " WHERE m.status='NOW' ORDER BY m.score DESC, m.box_office DESC LIMIT #{limit}")
    List<Map<String, Object>> scoreMovies(int limit);

    @Select(MOVIE_SELECT + " ORDER BY m.want_count DESC LIMIT #{limit}")
    List<Map<String, Object>> wantMovies(int limit);

    @Select("""
        <script>
        <bind name="offset" value="(page - 1) * size"/>
        """ + MOVIE_SELECT + """
        <where>
          <if test="status != null and status != ''">AND m.status = #{status}</if>
          <if test="keyword != null and keyword != ''">
            AND (m.title LIKE CONCAT('%', #{keyword}, '%') OR m.director LIKE CONCAT('%', #{keyword}, '%')
              OR m.english_title LIKE CONCAT('%', #{keyword}, '%'))
          </if>
          <if test="genreId != null">
            AND EXISTS (SELECT 1 FROM movie_genres mg2 WHERE mg2.movie_id=m.id AND mg2.genre_id=#{genreId})
          </if>
        </where>
        ORDER BY m.is_featured DESC, m.status='NOW' DESC, m.score DESC, m.release_date DESC
        LIMIT #{offset}, #{size}
        </script>
        """)
    List<Map<String, Object>> listMovies(@Param("status") String status, @Param("keyword") String keyword,
                                         @Param("genreId") Long genreId, @Param("page") int page,
                                         @Param("size") int size);

    @Select("""
        <script>
        SELECT COUNT(*) FROM movies m
        <where>
          <if test="status != null and status != ''">AND m.status = #{status}</if>
          <if test="keyword != null and keyword != ''">
            AND (m.title LIKE CONCAT('%', #{keyword}, '%') OR m.director LIKE CONCAT('%', #{keyword}, '%'))
          </if>
          <if test="genreId != null">
            AND EXISTS (SELECT 1 FROM movie_genres mg2 WHERE mg2.movie_id=m.id AND mg2.genre_id=#{genreId})
          </if>
        </where>
        </script>
        """)
    int countMovies(@Param("status") String status, @Param("keyword") String keyword, @Param("genreId") Long genreId);

    @Select(MOVIE_SELECT + " WHERE m.id=#{id}")
    Map<String, Object> findMovie(Long id);

    @Select("""
        SELECT g.id, g.name, g.color FROM genres g
        JOIN movie_genres mg ON mg.genre_id=g.id
        WHERE mg.movie_id=#{movieId} ORDER BY g.sort_no
        """)
    List<Map<String, Object>> movieGenres(Long movieId);

    @Select("""
        SELECT id, movie_id AS movieId, name, role_name AS roleName, avatar, sort_no AS sortNo
        FROM movie_cast WHERE movie_id=#{movieId} ORDER BY sort_no
        """)
    List<Map<String, Object>> movieCast(Long movieId);

    @Select("""
        SELECT r.id, r.score, r.content, DATE_FORMAT(r.created_at, '%Y-%m-%d') AS createdAt,
               u.name AS userName, u.avatar AS userAvatar
        FROM reviews r JOIN users u ON u.id=r.user_id
        WHERE r.movie_id=#{movieId} ORDER BY r.created_at DESC LIMIT 20
        """)
    List<Map<String, Object>> movieReviews(Long movieId);

    @Select("SELECT COUNT(*) FROM favorites WHERE movie_id=#{movieId}")
    int favoriteCount(Long movieId);

    @Select("""
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               m.title AS movieTitle, m.poster, m.duration,
               c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName, r.seat_rows AS seatRows, r.seat_cols AS seatCols
        FROM showings s
        JOIN movies m ON m.id=s.movie_id
        JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        WHERE s.movie_id=#{movieId} AND s.approval_status='APPROVED'
          AND s.start_time > NOW() AND c.status='APPROVED'
        ORDER BY s.start_time, s.price
        """)
    List<Map<String, Object>> movieShowings(@org.apache.ibatis.annotations.Param("movieId") Long movieId);

    @Select("""
        SELECT id, name, color, sort_no AS sortNo FROM genres ORDER BY sort_no, id
        """)
    List<Map<String, Object>> genres();

    @Select("""
        SELECT c.id, c.name, c.brand, c.city, c.address, c.phone, c.cover, c.intro,
               c.facilities, c.status, c.owner_id AS ownerId,
               (SELECT COUNT(*) FROM rooms r WHERE r.cinema_id=c.id AND r.status=1) AS roomCount,
               (SELECT COUNT(DISTINCT s.movie_id) FROM showings s
                 WHERE s.cinema_id=c.id AND s.approval_status='APPROVED' AND s.start_time>NOW()) AS movieCount
        FROM cinemas c
        WHERE c.status='APPROVED'
          AND (#{keyword} IS NULL OR #{keyword} = '' OR c.name LIKE CONCAT('%', #{keyword}, '%')
               OR c.address LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY c.id
        """)
    List<Map<String, Object>> cinemas(String keyword);

    @Select("""
        SELECT c.id, c.name, c.brand, c.city, c.address, c.phone, c.cover, c.intro,
               c.facilities, c.status, c.owner_id AS ownerId,
               (SELECT COUNT(*) FROM rooms r WHERE r.cinema_id=c.id AND r.status=1) AS roomCount,
               (SELECT COUNT(DISTINCT s.movie_id) FROM showings s
                 WHERE s.cinema_id=c.id AND s.approval_status='APPROVED' AND s.start_time>NOW()) AS movieCount
        FROM cinemas c WHERE c.id=#{id}
        """)
    Map<String, Object> findCinema(Long id);

    @Select("""
        <script>
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               m.title AS movieTitle, m.poster, m.score, m.duration,
               c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName, r.seat_rows AS seatRows, r.seat_cols AS seatCols
        FROM showings s
        JOIN movies m ON m.id=s.movie_id
        JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        WHERE s.cinema_id=#{cinemaId} AND s.approval_status='APPROVED' AND s.start_time>NOW()
        <if test="date != null and date != ''">AND DATE(s.start_time)=#{date}</if>
        ORDER BY s.start_time
        </script>
        """)
    List<Map<String, Object>> cinemaShowings(@Param("cinemaId") Long cinemaId, @Param("date") String date);

    @Select("""
        SELECT s.id, s.movie_id AS movieId, s.cinema_id AS cinemaId, s.room_id AS roomId,
               DATE_FORMAT(s.start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(s.end_time, '%Y-%m-%d %H:%i') AS endTime,
               s.price, s.language, s.version, s.approval_status AS approvalStatus,
               m.title AS movieTitle, m.poster, m.score, m.duration, m.synopsis,
               c.name AS cinemaName, c.address AS cinemaAddress,
               r.name AS roomName, r.seat_rows AS seatRows, r.seat_cols AS seatCols
        FROM showings s
        JOIN movies m ON m.id=s.movie_id
        JOIN cinemas c ON c.id=s.cinema_id
        JOIN rooms r ON r.id=s.room_id
        WHERE s.id=#{id}
        """)
    Map<String, Object> findShowing(Long id);

    @Select("""
        SELECT os.row_no AS rowNo, os.col_no AS colNo
        FROM order_seats os JOIN orders o ON o.id=os.order_id
        WHERE os.showing_id=#{showingId}
          AND (o.status IN ('PAID','USED') OR (o.status='UNPAID' AND o.created_at > DATE_SUB(NOW(), INTERVAL 10 MINUTE)))
        """)
    List<Map<String, Object>> occupiedSeats(Long showingId);

    @Select("""
        SELECT id, title, content, type, DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM notices WHERE status=1 ORDER BY created_at DESC LIMIT #{limit}
        """)
    List<Map<String, Object>> notices(int limit);

    @Select("SELECT COALESCE(SUM(amount),0) FROM orders WHERE status IN ('PAID','USED') AND DATE(created_at)=CURDATE()")
    java.math.BigDecimal todayBoxOffice();

    @Select("SELECT COUNT(*) FROM orders WHERE status IN ('PAID','USED') AND DATE(created_at)=CURDATE()")
    int todayOrders();

    @Select("SELECT COUNT(*) FROM users WHERE role='USER'")
    int userCount();

    @Select("SELECT COUNT(*) FROM showings WHERE approval_status='APPROVED' AND start_time>NOW()")
    int showingCount();
}
