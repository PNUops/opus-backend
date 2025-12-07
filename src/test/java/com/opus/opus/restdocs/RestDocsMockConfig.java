package com.opus.opus.restdocs;

import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.MemberQueryService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RestDocsMockConfig {

    @Bean
    public MemberCommandService memberCommandService() {
        return Mockito.mock(MemberCommandService.class);
    }

    @Bean
    public MemberQueryService memberQueryService() {
        return Mockito.mock(MemberQueryService.class);
    }

}
