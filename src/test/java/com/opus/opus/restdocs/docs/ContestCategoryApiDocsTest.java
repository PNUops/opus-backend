package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.CATEGORY_NAME_ALREADY_EXIST;
import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.exception.ContestCategoryException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestCategoryApiDocsTest extends RestDocsTest {

    private Member admin;
    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    private ContestCategoryRequest request;

    @BeforeEach
    void setUp() {
        this.admin = MemberFixture.createMember();
        setField(admin, "id", 1L);

        request = new ContestCategoryRequest("캡스톤");
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 카테고리 생성은 성공한다.")
    void 유효한_요청이면_대회_카테고리_생성은_성공한다() throws Exception {
        doNothing().when(contestCategoryCommandService).createCategory(any());

        mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-contest-category",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("categoryName", "카테고리 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 카테고리 이름이 존재한다면 에러를 반환한다.")
    void 이미_카테코리_이름이_존재한다면_에러를_반환한다() throws Exception {
        willThrow(new ContestCategoryException(CATEGORY_NAME_ALREADY_EXIST)).given(contestCategoryCommandService)
                .createCategory(any());

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andDo(document("create-contest-category-fail",
                        requestFields(
                                stringFieldWithPath("categoryName", "이미 존재하는 카테고리 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 카테고리 수정은 성공한다.")
    void 유효한_요청이면_대회_카테고리_수정은_성공한다() throws Exception {
        doNothing().when(contestCategoryCommandService).updateCategory(any(), any());

        mockMvc.perform(patch("/categories/{categoryId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-category",
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("categoryName", "카테고리 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 카테고리 삭제는 성공한다.")
    void 유효한_요청이면_대회_카테고리_삭제는_성공한다() throws Exception {
        doNothing().when(contestCategoryCommandService).deleteCategory(any());

        mockMvc.perform(delete("/categories/{categoryId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-contest-category",
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 카테고리 전체 조회는 성공한다.")
    void 유효한_요청이면_대회_카테고리_전체_조회는_성공한다() throws Exception {
        final List<ContestCategoryResponse> responses = List.of(
                new ContestCategoryResponse(1L, "해커톤", now()),
                new ContestCategoryResponse(2L, "자유대회", now())
        );

        when(contestCategoryQueryService.getAllContestCategories()).thenReturn(responses);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andDo(document("get-all-contest-category",
                        responseFields(
                                arrayFieldWithPath("[]", "대회 카테고리 목록"),
                                numberFieldWithPath("[].categoryId", "카테고리 ID"),
                                stringFieldWithPath("[].categoryName", "카테고리 이름"),
                                dateTimeFieldWithPath("[].updatedAt", "수정 일시")
                        )
                ));
    }
}
