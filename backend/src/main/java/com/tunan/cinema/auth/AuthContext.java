package com.tunan.cinema.auth;

public final class AuthContext {
    private static final ThreadLocal<Principal> HOLDER = new ThreadLocal<>();

    private AuthContext() {}

    public static void set(Principal principal) { HOLDER.set(principal); }
    public static Principal get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public record Principal(Long id, String username, String name, String role) {}
}
