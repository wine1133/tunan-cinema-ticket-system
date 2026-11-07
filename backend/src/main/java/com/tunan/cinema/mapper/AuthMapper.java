package com.tunan.cinema.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AuthMapper {
    @Select("""
        SELECT id, username, password, name, phone, email, avatar, role, status,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM users WHERE username = #{username} LIMIT 1
        """)
    Map<String, Object> findByUsername(String username);

    @Select("""
        SELECT id, username, name, phone, email, avatar, role, status,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM users WHERE id = #{id}
        """)
    Map<String, Object> findById(Long id);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countUsername(String username);

    @Insert("""
        INSERT INTO users(username, password, name, phone, email, role, status)
        VALUES(#{username}, #{password}, #{name}, #{phone}, #{email}, #{role}, 1)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(Map<String, Object> data);

    @Update("""
        UPDATE users SET name=#{name}, phone=#{phone}, email=#{email}, avatar=#{avatar}
        WHERE id=#{id}
        """)
    int updateProfile(@Param("id") Long id, @Param("name") String name,
                      @Param("phone") String phone, @Param("email") String email,
                      @Param("avatar") String avatar);

    @Update("UPDATE users SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select("""
        SELECT id, username, name, phone, email, avatar, role, status,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createdAt
        FROM users WHERE role=#{role} ORDER BY id DESC
        """)
    List<Map<String, Object>> listByRole(String role);

    @Update("UPDATE users SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("""
        UPDATE users SET name=#{name}, phone=#{phone}, email=#{email}, role=#{role}, status=#{status}
        WHERE id=#{id}
        """)
    int updateUser(Map<String, Object> data);

    @Insert("""
        INSERT INTO cinemas(name, brand, city, address, phone, intro, facilities, status, owner_id)
        VALUES(#{name}, #{brand}, #{city}, #{address}, #{phone}, #{intro}, #{facilities}, 'PENDING', #{ownerId})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCinema(Map<String, Object> data);
}
