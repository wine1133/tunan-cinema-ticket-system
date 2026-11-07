package com.tunan.cinema.auth;

import com.tunan.cinema.common.BizException;
import com.tunan.cinema.common.PublicApi;
import com.tunan.cinema.common.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (!(handler instanceof HandlerMethod method)) return true;

        boolean isPublic = method.hasMethodAnnotation(PublicApi.class)
                || method.getBeanType().isAnnotationPresent(PublicApi.class);
        if (isPublic) return true;

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException("请先登录后继续操作", 401);
        }
        AuthContext.set(jwtService.parse(auth.substring(7)));

        RequireRole role = method.getMethodAnnotation(RequireRole.class);
        if (role == null) role = method.getBeanType().getAnnotation(RequireRole.class);
        if (role != null) {
            String current = AuthContext.get().role();
            boolean allowed = Arrays.stream(role.value()).anyMatch(r -> r.equals(current));
            if (!allowed) throw new BizException("当前账号没有操作权限", 403);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
