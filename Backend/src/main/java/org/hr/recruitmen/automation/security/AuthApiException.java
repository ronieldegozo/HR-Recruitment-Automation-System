package org.hr.recruitmen.automation.security;

public final class AuthApiException extends RuntimeException {
    private final int status;

    public AuthApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}