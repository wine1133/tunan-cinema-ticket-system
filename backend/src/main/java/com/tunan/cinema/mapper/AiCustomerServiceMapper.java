package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiCustomerServiceMapper {
    @Insert("""
        INSERT INTO customer_service_messages(session_id, user_id, role, content, source)
        VALUES(#{sessionId}, #{userId}, #{role}, #{content}, #{source})
        """)
    int insertMessage(@Param("sessionId") String sessionId,
                      @Param("userId") Long userId,
                      @Param("role") String role,
                      @Param("content") String content,
                      @Param("source") String source);

    @Select("""
        SELECT role, content, source,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS createdAt
        FROM customer_service_messages
        WHERE session_id=#{sessionId}
        ORDER BY id DESC
        LIMIT #{limit}
        """)
    List<Map<String, Object>> recentMessages(@Param("sessionId") String sessionId,
                                             @Param("limit") int limit);
}