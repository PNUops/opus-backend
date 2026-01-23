package com.opus.opus.restdocs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.global.security.annotation.MemberArgumentResolver;
import com.opus.opus.helper.ApiTestHelper;
import com.opus.opus.modules.contest.api.ContestController;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.member.api.MemberController;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.api.TeamCommentController;
import com.opus.opus.modules.team.application.TeamCommentCommandService;
import com.opus.opus.modules.team.application.TeamCommentQueryService;
import com.opus.opus.modules.notice.api.NoticeController;
import com.opus.opus.modules.notice.application.NoticeCommandService;
import com.opus.opus.modules.notice.application.NoticeQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest({
        MemberController.class,
        ContestController.class,
        TeamCommentController.class,
        NoticeController.class,
        ContestController.class,
})
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest extends ApiTestHelper {

    // Service
    @MockitoBean
    protected MemberCommandService memberCommandService;

    @MockitoBean
    protected MemberQueryService memberQueryService;

    @MockitoBean
    protected TeamCommentCommandService teamCommentCommandService;

    @MockitoBean
    protected TeamCommentQueryService teamCommentQueryService;

    @MockitoBean
    protected NoticeCommandService noticeCommandService;

    @MockitoBean
    protected NoticeQueryService noticeQueryService;

    @MockitoBean
    protected ContestCommandService contestCommandService;

    @MockitoBean
    protected ContestQueryService contestQueryService;

    // Setting
    @Autowired
    protected WebApplicationContext context;

    @MockitoBean
    protected JwtProvider jwtProvider;

    @MockitoBean
    protected MemberRepository memberRepository;

    @MockitoBean
    protected MemberArgumentResolver memberArgumentResolver;

    @BeforeEach
    void setUp(final RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint())
                )
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    protected FieldDescriptor stringFieldWithPath(final String path, final String description) {
        return fieldWithPath(path).type(JsonFieldType.STRING).description(description);
    }

    protected FieldDescriptor numberFieldWithPath(final String path, final String description) {
        return fieldWithPath(path).type(JsonFieldType.NUMBER).description(description);
    }

    protected FieldDescriptor booleanFieldWithPath(final String path, final String description) {
        return fieldWithPath(path).type(JsonFieldType.BOOLEAN).description(description);
    }

    protected FieldDescriptor arrayFieldWithPath(final String path, final String description) {
        return fieldWithPath(path).type(JsonFieldType.ARRAY).description(description);
    }

    protected FieldDescriptor dateTimeFieldWithPath(final String path, final String description) {
        return fieldWithPath(path).type(JsonFieldType.STRING).description(description + " (ISO-8601)");
    }
}
