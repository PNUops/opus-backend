package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.domain.SortType.ASC;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CONTEST_NAME_ALREADY_EXIST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_TEAM_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_CONTEST_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE;
import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestVotesLimitRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class ContestApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";
    private String authorizationHeaderDescription;
    private byte[] testImage;

    @BeforeEach
    void setUp() {
        authorizationHeaderDescription = "Bearer %s.access.token";
        testImage = "test-image-content".getBytes();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 배너 이미지를 조회하면 실패한다.")
    void 대회의_배너_이미지_조회_실패_대회없음() throws Exception {
        // Given
        final Long contestId = 999L;

        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getContestBanner(any());

        // When & Then
        mockMvc.perform(get("/contests/{contestId}/image/banner", contestId))
                .andExpect(status().isNotFound())
                .andDo(document("get-contest-banner-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 등록되지 않은 대회의 배너 이미지를 조회하면 실패한다.")
    void 대회의_배너_이미지_조회_실패_이미지없음() throws Exception {
        // Given
        final Long contestId = 1L;

        willThrow(new FileException(FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID))
                .given(contestQueryService)
                .getContestBanner(any());

        // When & Then
        mockMvc.perform(get("/contests/{contestId}/image/banner", contestId))
                .andExpect(status().isNotFound())
                .andDo(document("get-contest-banner-fail-image-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 변환 중인 대회의 배너 이미지를 조회하면 실패한다.")
    void 대회의_배너_이미지_조회_실패_변환중() throws Exception {
        // Given
        final Long contestId = 1L;

        willThrow(new FileException(FileExceptionType.NOT_WEBP_CONVERTED))
                .given(contestQueryService)
                .getContestBanner(any());

        // When & Then
        mockMvc.perform(get("/contests/{contestId}/image/banner", contestId))
                .andExpect(status().isAccepted())
                .andDo(document("get-contest-banner-fail-converting",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 투표 기간을 조회하면 시작일과 종료일을 반환한다.")
    void 투표_기간을_조회하면_시작일과_종료일을_반환한다() throws Exception {
        VotePeriodResponse response = new VotePeriodResponse(
                LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0, 0)
        );

        given(contestQueryService.getVotePeriod(any())).willReturn(response);

        mockMvc.perform(get("/contests/{contestId}/vote", 1L))
                .andExpect(status().isOk())
                .andDo(document("get-vote-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        responseFields(
                                dateTimeFieldWithPath("voteStartAt", "투표 시작일"),
                                dateTimeFieldWithPath("voteEndAt", "투표 종료일")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 최대 투표 개수가 정상적으로 설정된다.")
    void 유효한_요청이면_최대_투표_개수가_정상적으로_설정된다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        doNothing().when(contestCommandService).updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-max-votes-limit",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 관리자는 투표 기간을 수정할 수 있다.")
    void 관리자는_투표_기간을_수정할_수_있다() throws Exception {
        VoteUpdateRequest request = new VoteUpdateRequest(
                LocalDateTime.of(2026, 2, 1, 0, 0, 0),
                LocalDateTime.of(2026, 2, 10, 0, 0, 0)
        );

        doNothing().when(contestCommandService).updateVotePeriod(any(), any());

        mockMvc.perform(put("/contests/{contestId}/vote", 1L)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-vote-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                dateTimeFieldWithPath("voteStartAt", "투표 시작일"),
                                dateTimeFieldWithPath("voteEndAt", "투표 종료일")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수 설정 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_최대_투표_개수_설정_시_에러를_반환한다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestCommandService)
                .updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("update-max-votes-limit-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 진행 중 최대 투표 개수 변경 시 400 에러를 반환한다.")
    void 투표_진행_중_최대_투표_개수_변경_시_에러를_반환한다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        willThrow(new ContestException(NOT_ALLOWED_DURING_VOTING_PERIOD))
                .given(contestCommandService)
                .updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-max-votes-limit-fail-voting-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수를 정상적으로 조회할 수 있다.")
    void 최대_투표_개수를_정상적으로_조회할_수_있다() throws Exception {
        final ContestVotesLimitResponse response = new ContestVotesLimitResponse(2);

        when(contestQueryService.getMaxVotesLimit(any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-max-votes-limit",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_최대_투표_개수_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getMaxVotesLimit(any());

        mockMvc.perform(get("/contests/{contestId}/votes", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-max-votes-limit-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 배너 이미지를 조회한다.")
    void 대회의_배너_이미지를_조회한다() throws Exception {
        // Given
        final Long contestId = 1L;
        final ImageResponse response = new ImageResponse(new ByteArrayResource(testImage), "image/png");

        when(contestQueryService.getContestBanner(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/contests/{contestId}/image/banner", contestId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andDo(document("get-contest-banner",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 배너 이미지를 등록한다.")
    void 대회의_배너_이미지를_등록한다() throws Exception {
        // Given
        final Long contestId = 1L;
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "banner.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doNothing().when(contestCommandService).saveBannerImage(any(), any());

        // When & Then
        mockMvc.perform(multipart("/contests/{contestId}/image/banner", contestId)
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("save-contest-banner",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestParts(
                                partWithName("image").description("등록할 배너 이미지 (모든 이미지 형식 지원)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 배너 이미지를 삭제한다.")
    void 대회의_배너_이미지를_삭제한다() throws Exception {
        // Given
        final Long contestId = 1L;
        doNothing().when(contestCommandService).deleteBannerImage(any());

        // When & Then
        mockMvc.perform(delete("/contests/{contestId}/image/banner", contestId)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-contest-banner",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 생성은 성공한다.")
    void 유효한_요청이면_대회_카테고리_생성은_성공한다() throws Exception {
        final ContestRequest request = new ContestRequest("제6회 해커톤", 1L);
        final ContestResponse response = new ContestResponse(1L, request.contestName(), request.categoryId(), "해커톤",
                true, now());

        when(contestCommandService.createContest(any())).thenReturn(response);

        mockMvc.perform(post("/contests")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("create-contest",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                stringFieldWithPath("contestName", "대회 이름"),
                                numberFieldWithPath("categoryId", "카테고리 ID")
                        ),
                        responseFields(
                                numberFieldWithPath("contestId", "대회 ID"),
                                stringFieldWithPath("contestName", "대회 이름"),
                                numberFieldWithPath("categoryId", "카테고리 ID"),
                                stringFieldWithPath("categoryName", "카테고리 이름"),
                                booleanFieldWithPath("isCurrent", "현재 진행 대회 여부"),
                                dateTimeFieldWithPath("updatedAt", "수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 대회 이름이 존재한다면 에러를 반환한다.")
    void 이미_대회_이름이_존재한다면_에러를_반환한다() throws Exception {
        final ContestRequest request = new ContestRequest("제6회 해커톤", 1L);

        willThrow(new ContestException(CONTEST_NAME_ALREADY_EXIST)).given(contestCommandService).createContest(any());

        mockMvc.perform(post("/contests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andDo(document("create-contest-fail",
                        requestFields(
                                stringFieldWithPath("contestName", "이미 존재하는 대회 이름"),
                                numberFieldWithPath("categoryId", "카테고리 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 수정은 성공한다.")
    void 유효한_요청이면_대회_수정은_성공한다() throws Exception {
        final ContestRequest request = new ContestRequest("제6회 해커톤", 1L);

        doNothing().when(contestCommandService).updateContest(any(), any());

        mockMvc.perform(patch("/contests/{contestId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                stringFieldWithPath("contestName", "대회 이름"),
                                numberFieldWithPath("categoryId", "카테고리 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 삭제는 성공한다.")
    void 유효한_요청이면_대회_삭제는_성공한다() throws Exception {
        doNothing().when(contestCommandService).deleteContest(any());

        mockMvc.perform(delete("/contests/{contestId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-contest",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 전체 조회는 성공한다.")
    void 유효한_요청이면_대회_전체_조회는_성공한다() throws Exception {
        final List<ContestResponse> responses = List.of(
                new ContestResponse(1L, "제6회 해커톤", 1L, "해커톤", true, now()),
                new ContestResponse(2L, "CSE 캡스톤", 2L, "캡스톤", false, now())
        );

        when(contestQueryService.getAllContests()).thenReturn(responses);

        mockMvc.perform(get("/contests"))
                .andExpect(status().isOk())
                .andDo(document("get-all-contest",
                        responseFields(
                                arrayFieldWithPath("[]", "대회 목록"),
                                numberFieldWithPath("[].contestId", "대회 ID"),
                                stringFieldWithPath("[].contestName", "대회 이름"),
                                numberFieldWithPath("[].categoryId", "카테고리 ID"),
                                stringFieldWithPath("[].categoryName", "카테고리 이름"),
                                booleanFieldWithPath("[].isCurrent", "현재 진행 대회 여부"),
                                dateTimeFieldWithPath("[].updatedAt", "수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 정렬 설정 변경은 성공한다.")
    void 유효한_요청이면_대회의_정렬_설정_변경은_성공한다() throws Exception {
        final ContestSortRequest request = new ContestSortRequest(ASC);

        doNothing().when(contestCommandService).updateContestSort(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-sort",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                stringFieldWithPath("mode", "수정할 대회 정렬 모드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 정렬 설정 조회는 성공한다.")
    void 유효한_요청이면_대회의_정렬_설정_조회는_성공한다() throws Exception {
        final ContestSortResponse response = new ContestSortResponse(ASC);
        when(contestQueryService.getContestSort(any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/sort", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-contest-sort",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                stringFieldWithPath("currentMode", "현재 적용되어 있는 모드 정보")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 수동 정렬 설정 순서 저장은 성공한다.")
    void 유효한_요청이면_대회_수동_정렬_순서_저장은_성공한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(3L, 2));

        doNothing().when(contestCommandService).updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-sort-custom",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                arrayFieldWithPath("[]", "정렬 순서를 담은 팀 배열(모든 팀 다 보내주세요)"),
                                numberFieldWithPath("[].teamId", "정렬 순서를 변경할 팀 ID"),
                                numberFieldWithPath("[].itemOrder", "팀의 정렬 순서 (1부터 팀 개수까지)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] CUSTOM모드가 아니라면 수동 정렬 저장은 실패한다.")
    void CUSTOM모드가_아니라면_수동_정렬_저장은_실패한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(3L, 2));

        willThrow(new ContestException(ONLY_CUSTOM_MODE_CAN_CHANGE)).given(contestCommandService)
                .updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden())
                .andDo(document("update-contest-sort-custom-fail-mode"));
    }

    @Test
    @DisplayName("[실패] request에 중복 teamId가 있으면 수동 정렬 저장은 실패한다.")
    void request에_중복_teamId가_있으면_수동_정렬_저장은_실패한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(1L, 2));

        willThrow(new ContestException(DUPLICATE_TEAM_ID_IN_SORT_REQUEST)).given(contestCommandService)
                .updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-contest-sort-custom-fail-duplicate-teamId",
                        requestFields(
                                arrayFieldWithPath("[]", "정렬 순서를 담은 팀 배열"),
                                numberFieldWithPath("[].teamId", "팀 ID(중복 존재)"),
                                numberFieldWithPath("[].itemOrder", "팀의 정렬 순서")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] request에 중복 itemOrder가 있으면 수동 정렬 저장은 실패한다.")
    void request에_중복_itemOrder가_있으면_수동_정렬_저장은_실패한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(3L, 1));

        willThrow(new ContestException(DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST)).given(contestCommandService)
                .updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-contest-sort-custom-fail-duplicate-itemOrder",
                        requestFields(
                                arrayFieldWithPath("[]", "정렬 순서를 담은 팀 배열"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                numberFieldWithPath("[].itemOrder", "팀의 정렬 순서(중복 존재)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 요청 팀 개수와 저장된 팀 개수가 다르면 수동 정렬 저장은 실패한다.")
    void 요청_팀_개수와_저장된_팀_개수가_다르면_수동_정렬_저장은_실패한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(3L, 2));

        willThrow(new ContestException(INVALID_CONTEST_SORT_CUSTOM_REQUEST)).given(contestCommandService)
                .updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-contest-sort-custom-fail-different-size"));
    }

    @Test
    @DisplayName("[실패] 저장된 팀 개수보다 itemOrder가 크면 수동 정렬 저장은 실패한다.")
    void 저장된_팀_개수보다_itemOrder가_크면_수동_정렬_저장은_실패한다() throws Exception {
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(1L, 1),
                new ContestSortCustomRequest(2L, 3), new ContestSortCustomRequest(3L, 2));

        willThrow(new ContestException(INVALID_ITEM_ORDER)).given(contestCommandService)
                .updateContestSortCustom(any(), any());

        mockMvc.perform(put("/contests/{contestId}/sort/custom", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-contest-sort-custom-fail-over-itemOrder"));
    }

    @Test
    @DisplayName("[성공] 정상적인 요청이면 투표_로그가_최신순으로_조회된다.")
    void 정상적인_요청이면_투표_로그가_최신순으로_조회된다() throws Exception {
        final List<ContestVoteLogResponse> content = List.of(
                new ContestVoteLogResponse("이옵스", "lee@pusan.ac.kr", "teamA", now()),
                new ContestVoteLogResponse("김옵스", "kim@pusan.ac.kr", "teamB", now().minusSeconds(1)));

        final Page<ContestVoteLogResponse> page = new PageImpl<>(content,
                PageRequest.of(0, 20, Sort.by(DESC, "votedAt")), 2);

        when(contestQueryService.getContestVoteLog(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/contests/{contestId}/vote-log", 1)
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-contest-vote-log",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                arrayFieldWithPath("content[]", "투표 로그 목록 (최신순)"),
                                stringFieldWithPath("content[].voterName", "투표자 이름"),
                                stringFieldWithPath("content[].voterEmail", "투표자 이메일"),
                                stringFieldWithPath("content[].teamName", "투표한 팀 이름"),
                                dateTimeFieldWithPath("content[].votedAt", "투표 시점"),

                                subsectionFieldWithPath("pageable", "페이지 정보"),
                                booleanFieldWithPath("last", "마지막 페이지 여부"),
                                numberFieldWithPath("totalPages", "전체 페이지 수"),
                                numberFieldWithPath("totalElements", "전체 요소 수"),
                                booleanFieldWithPath("first", "첫 페이지 여부"),
                                numberFieldWithPath("size", "페이지 크기"),
                                numberFieldWithPath("number", "현재 페이지 번호"),
                                subsectionFieldWithPath("sort", "정렬 정보"),
                                numberFieldWithPath("numberOfElements", "현재 페이지 요소 수"),
                                booleanFieldWithPath("empty", "비어있는 페이지 여부"))));
    }

    @Test
    @DisplayName("[성공] 대회의 투표 랭킹을 조회할 수 있다.")
    void 대회의_투표_랭킹을_조회할_수_있다() throws Exception {
        final List<ContestRankingResponse> responses = List.of(
                new ContestRankingResponse(1, 10L, "팀 A", "AI 번역기", "AI 트랙", 150L),
                new ContestRankingResponse(2, 11L, "팀 B", "헬스 분석기", "헬스케어 트랙", 120L),
                new ContestRankingResponse(2, 12L, "팀 C", "노인 케어봇", "AI 트랙", 120L),
                new ContestRankingResponse(3, 13L, "팀 D", "감정 분석기", "AI 트랙", 85L)
        );

        when(contestQueryService.getTeamRanking(any())).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/ranking", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-team-ranking",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "팀 랭킹 목록"),
                                numberFieldWithPath("[].rank", "투표 기준 순위 (Dense Ranking)"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                stringFieldWithPath("[].trackName", "트랙/분과명"),
                                numberFieldWithPath("[].voteCount", "투표 수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 투표 랭킹 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_투표_랭킹_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getTeamRanking(any());

        mockMvc.perform(get("/contests/{contestId}/ranking", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-ranking-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 투표 집계를 조회할 수 있다.")
    void 대회의_투표_집계를_조회할_수_있다() throws Exception {
        final ContestVoteStatisticsResponse response = new ContestVoteStatisticsResponse(366L, 249L, 1.5);

        when(contestQueryService.getVoteStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/votes/statistics", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-vote-statistics",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                numberFieldWithPath("totalVotes", "총 투표 수"),
                                numberFieldWithPath("totalVoters", "투표한 사람 수"),
                                numberFieldWithPath("averageVotesPerVoter", "1인당 평균 투표 수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 투표 집계 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_투표_집계_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getVoteStatistics(any());

        mockMvc.perform(get("/contests/{contestId}/votes/statistics", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-vote-statistics-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 팀별 프로젝트 등록 현황을 조회할 수 있다.")
    void 대회의_팀별_프로젝트_등록_현황을_조회할_수_있다() throws Exception {
        final List<ContestSubmissionResponse> responses = List.of(
                new ContestSubmissionResponse(1L, "딥러닝 드리머즈", "AI 음성 번역기", "소프트웨어/인공지능", true),
                new ContestSubmissionResponse(2L, "패킷 마스터즈", "실시간 트래픽 분석기", "네트워크/통신", true),
                new ContestSubmissionResponse(3L, "시큐리티 가디언즈", "IoT 취약점 스캐너", "하드웨어/보안", false)
        );

        when(contestQueryService.getTeamSubmissions(any())).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/submissions", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-team-submissions",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "팀별 프로젝트 등록 현황 목록"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                stringFieldWithPath("[].trackName", "트랙/분과명"),
                                booleanFieldWithPath("[].isSubmitted", "프로젝트 제출 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 프로젝트 등록 현황 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_프로젝트_등록_현황_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getTeamSubmissions(any());

        mockMvc.perform(get("/contests/{contestId}/submissions", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-submissions-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 템플릿을 조회한다.")
    void 대회의_템플릿_설정을_조회한다() throws Exception {
        final ContestTemplateResponse response = new ContestTemplateResponse(
                true, true, true, true, true, true,
                true, true, true, true, true, true
        );

        when(contestQueryService.getContestTemplate(any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/template", 1))
                .andExpect(status().isOk())
                .andDo(document("get-contest-template",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        responseFields(
                                booleanFieldWithPath("trackRequired", "트랙/분과 필수 여부"),
                                booleanFieldWithPath("projectNameRequired", "프로젝트명 필수 여부"),
                                booleanFieldWithPath("teamNameRequired", "팀명 필수 여부"),
                                booleanFieldWithPath("leaderRequired", "팀장 필수 여부"),
                                booleanFieldWithPath("teamMembersRequired", "팀원 필수 여부"),
                                booleanFieldWithPath("professorRequired", "지도교수 필수 여부"),
                                booleanFieldWithPath("githubPathRequired", "GitHub 링크 필수 여부"),
                                booleanFieldWithPath("youTubePathRequired", "YouTube 링크 필수 여부"),
                                booleanFieldWithPath("productionPathRequired", "배포 링크 필수 여부"),
                                booleanFieldWithPath("overviewRequired", "프로젝트 개요 필수 여부"),
                                booleanFieldWithPath("posterRequired", "포스터 필수 여부"),
                                booleanFieldWithPath("imagesRequired", "이미지 필수 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 템플릿 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_템플릿_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getContestTemplate(any());

        mockMvc.perform(get("/contests/{contestId}/template", 999))
                .andExpect(status().isNotFound())
                .andDo(document("get-contest-template-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 대회의 템플릿을 수정한다.")
    void 대회의_템플릿_설정을_수정한다() throws Exception {
        final ContestTemplateRequest request = new ContestTemplateRequest(
                false, false, false, false, false, false,
                false, false, false, false, false, false
        );

        doNothing().when(contestCommandService).updateContestTemplate(any(), any());

        mockMvc.perform(put("/contests/{contestId}/template", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-template",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                booleanFieldWithPath("trackRequired", "트랙/분과 필수 여부"),
                                booleanFieldWithPath("projectNameRequired", "프로젝트명 필수 여부"),
                                booleanFieldWithPath("teamNameRequired", "팀명 필수 여부"),
                                booleanFieldWithPath("leaderRequired", "팀장 필수 여부"),
                                booleanFieldWithPath("teamMembersRequired", "팀원 필수 여부"),
                                booleanFieldWithPath("professorRequired", "지도교수 필수 여부"),
                                booleanFieldWithPath("githubPathRequired", "GitHub 링크 필수 여부"),
                                booleanFieldWithPath("youTubePathRequired", "YouTube 링크 필수 여부"),
                                booleanFieldWithPath("productionPathRequired", "배포 링크 필수 여부"),
                                booleanFieldWithPath("overviewRequired", "프로젝트 개요 필수 여부"),
                                booleanFieldWithPath("posterRequired", "포스터 필수 여부"),
                                booleanFieldWithPath("imagesRequired", "이미지 필수 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 템플릿 수정 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_템플릿_수정_시_에러를_반환한다() throws Exception {
        final ContestTemplateRequest request = new ContestTemplateRequest(
                false, false, false, false, false, false,
                false, false, false, false, false, false
        );

        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestCommandService)
                .updateContestTemplate(any(), any());

        mockMvc.perform(put("/contests/{contestId}/template", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("update-contest-template-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }
}
