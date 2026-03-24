package com.opus.opus.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class CryptoConfig {

    @Value("${aes.secret-key}")
    private String aesSecretKey;

    @Value("${aes.salt}")
    private String aesSalt;

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.delux(aesSecretKey, aesSalt);
    }
}
