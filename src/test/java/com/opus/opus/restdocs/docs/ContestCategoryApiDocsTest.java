package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.CATEGORY_NAME_ALREADY_EXIST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortRequest;
import com.opus.opus.modules.contest.application.dto.response.CategoryContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarCategorySortResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse.ContestItem;
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
    void 이미_카테고리_이름이_존재한다면_에러를_반환한다() throws Exception {
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

    @Test
    @DisplayName("[성공] 사이드바 조회는 카테고리별 대회 목록을 반환한다.")
    void 사이드바_조회는_카테고리별_대회_목록을_반환한다() throws Exception {
        final List<SidebarResponse> responses = List.of(
                new SidebarResponse(1L, "창의융합 SW해커톤", List.of(
                        new ContestItem(4L, "제6회 PNU 창의융합 SW해커톤", true),
                        new ContestItem(3L, "제5회 PNU 창의융합 SW해커톤", false)
                )),
                new SidebarResponse(2L, "졸업과제", List.of())
        );

        when(contestCategoryQueryService.getSidebar()).thenReturn(responses);

        mockMvc.perform(get("/sidebar"))
                .andExpect(status().isOk())
                .andDo(document("get-sidebar",
                        responseFields(
                                arrayFieldWithPath("[]", "카테고리 목록"),
                                numberFieldWithPath("[].categoryId", "카테고리 ID"),
                                stringFieldWithPath("[].categoryName", "카테고리 이름"),
                                arrayFieldWithPath("[].contests[]", "해당 카테고리의 대회 목록 (대회가 없으면 빈 배열)"),
                                numberFieldWithPath("[].contests[].contestId", "대회 ID"),
                                stringFieldWithPath("[].contests[].contestName", "대회 이름"),
                                booleanFieldWithPath("[].contests[].isCurrent", "현재 진행 중인 대회 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 정렬 모드 변경은 성공한다.")
    void 유효한_요청이면_카테고리_정렬_모드_변경은_성공한다() throws Exception {
        doNothing().when(contestCategoryCommandService).updateCategorySort(any());

        mockMvc.perform(put("/categories/sort")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SidebarCategorySortRequest(ASC))))
                .andExpect(status().isNoContent())
                .andDo(document("update-category-sort",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("mode", "수정할 카테고리 정렬 모드 (ASC, DESC, CUSTOM)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 정렬 모드 조회는 성공한다.")
    void 유효한_요청이면_카테고리_정렬_모드_조회는_성공한다() throws Exception {
        when(contestCategoryQueryService.getCategorySort()).thenReturn(new SidebarCategorySortResponse(ASC));

        mockMvc.perform(get("/categories/sort")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-category-sort",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                stringFieldWithPath("mode", "현재 적용되어 있는 카테고리 정렬 모드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 수동 정렬 순서 저장은 성공한다.")
    void 유효한_요청이면_카테고리_수동_정렬_순서_저장은_성공한다() throws Exception {
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(1L, 2),
                new SidebarCategorySortCustomRequest(2L, 1));

        doNothing().when(contestCategoryCommandService).updateCategorySortCustom(any());

        mockMvc.perform(put("/categories/sort/custom")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isNoContent())
                .andDo(document("update-category-sort-custom",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                arrayFieldWithPath("[]", "정렬 순서를 담은 카테고리 배열(모든 카테고리를 다 보내주세요)"),
                                numberFieldWithPath("[].categoryId", "정렬 순서를 변경할 카테고리 ID"),
                                numberFieldWithPath("[].itemOrder", "카테고리의 정렬 순서 (1부터 카테고리 개수까지)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] CUSTOM 모드가 아니라면 카테고리 수동 정렬 저장은 실패한다.")
    void CUSTOM모드가_아니라면_카테고리_수동_정렬_저장은_실패한다() throws Exception {
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(1L, 1));

        willThrow(new ContestCategoryException(ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT))
                .given(contestCategoryCommandService).updateCategorySortCustom(any());

        mockMvc.perform(put("/categories/sort/custom")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden())
                .andDo(document("update-category-sort-custom-fail-mode"));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 내 대회 정렬 모드 변경은 성공한다.")
    void 유효한_요청이면_카테고리_내_대회_정렬_모드_변경은_성공한다() throws Exception {
        doNothing().when(contestCategoryCommandService).updateContestSortInCategory(any(), any());

        mockMvc.perform(put("/categories/{categoryId}/contests/sort", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryContestSortRequest(ASC))))
                .andExpect(status().isNoContent())
                .andDo(document("update-category-contest-sort",
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("mode", "수정할 카테고리 내 대회 정렬 모드 (ASC, DESC, CUSTOM)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 내 대회 정렬 모드 조회는 성공한다.")
    void 유효한_요청이면_카테고리_내_대회_정렬_모드_조회는_성공한다() throws Exception {
        when(contestCategoryQueryService.getContestSortInCategory(any())).thenReturn(
                new CategoryContestSortResponse(ASC));

        mockMvc.perform(get("/categories/{categoryId}/contests/sort", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-category-contest-sort",
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                stringFieldWithPath("mode", "현재 적용되어 있는 카테고리 내 대회 정렬 모드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 카테고리 내 대회 수동 정렬 순서 저장은 성공한다.")
    void 유효한_요청이면_카테고리_내_대회_수동_정렬_순서_저장은_성공한다() throws Exception {
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(1L, 2),
                new CategoryContestSortCustomRequest(2L, 1));

        doNothing().when(contestCategoryCommandService).updateContestSortInCategoryCustom(any(), any());

        mockMvc.perform(put("/categories/{categoryId}/contests/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isNoContent())
                .andDo(document("update-category-contest-sort-custom",
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                arrayFieldWithPath("[]", "정렬 순서를 담은 대회 배열(해당 카테고리의 모든 대회를 다 보내주세요)"),
                                numberFieldWithPath("[].contestId", "정렬 순서를 변경할 대회 ID"),
                                numberFieldWithPath("[].itemOrder", "대회의 정렬 순서 (1부터 대회 개수까지)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] CUSTOM 모드가 아니라면 카테고리 내 대회 수동 정렬 저장은 실패한다.")
    void CUSTOM모드가_아니라면_카테고리_내_대회_수동_정렬_저장은_실패한다() throws Exception {
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(1L, 1));

        willThrow(new ContestCategoryException(ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT))
                .given(contestCategoryCommandService).updateContestSortInCategoryCustom(any(), any());

        mockMvc.perform(put("/categories/{categoryId}/contests/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden())
                .andDo(document("update-category-contest-sort-custom-fail-mode"));
    }
}
