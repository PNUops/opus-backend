package com.opus.opus.restdocs;

import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.MemberQueryService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class RestDocsMockConfig {

    @MockitoBean
    public MemberCommandService memberCommandService;

    @MockitoBean
    public MemberQueryService memberQueryService;

}
