package org.example.rollbackservice.portone;

public record PortOneTokenResponse(
        int code,
        String message,
        TokenResponse response
) {
    public record TokenResponse(
            String access_token,
            long now,
            long expired_at
    ) {}
}
