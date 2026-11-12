package com.bankapp.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory list of logged-out tokens (by jti). Entries are dropped once the token would have
 * expired anyway. Simple on purpose: it is cleared on restart and doesn't work across several servers.
 */
@Component
public class TokenBlacklist {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant tokenExpiry) {
        Instant now = Instant.now();
        revoked.values().removeIf(exp -> exp.isBefore(now));
        revoked.put(jti, tokenExpiry);
    }

    public boolean isRevoked(String jti) {
        return revoked.containsKey(jti);
    }
}
