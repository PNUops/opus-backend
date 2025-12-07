package com.opus.opus.restdocs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.opus.opus.helper.ApiTestHelper;
import com.opus.opus.modules.member.api.MemberController;
import com.opus.opus.modules.team.api.TeamController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest({
        MemberController.class,
        TeamController.class
})
@Import({RestDocsConfig.class, RestDocsMockConfig.class})
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest extends ApiTestHelper {

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    protected WebApplicationContext context;

    @BeforeEach
    void setUp(final RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(restDocs)
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
}
