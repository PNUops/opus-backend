package com.opus.opus.helper;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.global.util.AuthRedisUtil;
import com.opus.opus.global.util.CacheRedisUtil;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.global.util.GoogleTokenManager;
import com.opus.opus.global.util.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest extends ApiTestHelper {

    @Autowired
    protected AuthRedisUtil authRedisUtil;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtProvider jwtProvider;

    @MockitoBean
    private MailUtil mailUtil;

    @MockitoBean
    protected GoogleTokenManager googleTokenManager;

    @MockitoBean
    protected FileStorageUtil fileStorageUtil;

    @MockitoBean
    protected CacheRedisUtil cacheRedisUtil;

    @BeforeEach
    void setUp(final WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }
}
