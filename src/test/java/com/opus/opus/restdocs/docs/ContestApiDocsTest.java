package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.domain.SortType.ASC;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestVotesLimitRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
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

        willThrow(new ContestException(CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
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
                new ContestSortCustomRequest(1L, 3), new ContestSortCustomRequest(1L, 2));

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
    @DisplayName("[실패] request에 중복 teamId가 있으면 수동 정렬 저장은 실패한다.")
    void request에_중복_teamId가_있으면_수동_정렬_저장은_실패한다() throws Exception {

    }

    @Test
    @DisplayName("[실패] request에 중복 itemOrder가 있으면 수동 정렬 저장은 실패한다.")
    void request에_중복_itemOrder가_있으면_수동_정렬_저장은_실패한다() throws Exception {

    }

    @Test
    @DisplayName("[실패] 요청 팀 개수와 저장된 팀 개수가 다르면 수동 정렬 저장은 실패한다.")
    void 요청_팀_개수와_저장된_팀_개수가_다르면_수동_정렬_저장은_실패한다() throws Exception {

    }

    @Test
    @DisplayName("[실패] 저장된 팀 개수보다 itemOrder가 크면 수동 정렬 저장은 실패한다.")
    void 저장된_팀_개수보다_itemOrder가_크면_수동_정렬_저장은_실패한다() throws Exception {

    }
}
