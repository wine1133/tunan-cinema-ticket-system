package com.tunan.cinema.common;

public class BizException extends RuntimeException {
    private final int status;

    public BizException(String message) { this(message, 400); }

    public BizException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
