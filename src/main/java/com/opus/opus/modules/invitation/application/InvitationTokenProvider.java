package com.opus.opus.modules.invitation.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class InvitationTokenProvider {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        final byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(final String token) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
