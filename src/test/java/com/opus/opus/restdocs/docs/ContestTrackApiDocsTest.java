package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.TRACKNAME_DUPLICATED;
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
import com.opus.opus.modules.contest.application.dto.request.ContestTrackRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestTrackResponse;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestTrackApiDocsTest extends RestDocsTest {

    private Member admin;
    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    private ContestTrackRequest request;

    @BeforeEach
    void setUp() {
        this.admin = MemberFixture.createMember();
        setField(admin, "id", 1L);

        request = new ContestTrackRequest("창업");
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 분과 생성은 성공한다.")
    void 유효한_요청이면_대회_분과_생성은_성공한다() throws Exception {
        doNothing().when(contestTrackCommandService).createTrack(any(), any());

        mockMvc.perform(post("/contests/{contestId}/tracks", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-contest-track",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("trackName", "분과 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 분과 이름이 존재한다면 에러를 반환한다.")
    void 이미_분과_이름이_존재한다면_에러를_반환한다() throws Exception {
        willThrow(new ContestTrackException(TRACKNAME_DUPLICATED)).given(contestTrackCommandService)
                .createTrack(any(), any());

        mockMvc.perform(post("/contests/{contestId}/tracks", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andDo(document("create-contest-track-fail",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("trackName", "이미 존재하는 분과 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 분과 수정은 성공한다.")
    void 유효한_요청이면_대회_분과_수정은_성공한다() throws Exception {
        doNothing().when(contestTrackCommandService).updateTrack(any(), any(), any());

        mockMvc.perform(patch("/contests/{contestId}/tracks/{trackId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-track",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("trackId").description("분과 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("trackName", "카테고리 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 분과 삭제는 성공한다.")
    void 유효한_요청이면_대회_분과_삭제는_성공한다() throws Exception {
        doNothing().when(contestTrackCommandService).deleteTrack(any(), any());

        mockMvc.perform(delete("/contests/{contestId}/tracks/{trackId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-contest-track",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("trackId").description("분과 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 대회 카테고리 전체 조회는 성공한다.")
    void 유효한_요청이면_대회_카테고리_전체_조회는_성공한다() throws Exception {
        final List<ContestTrackResponse> responses = List.of(
                new ContestTrackResponse(1L, "창업", now()),
                new ContestTrackResponse(2L, "융합", now())
        );

        when(contestTrackQueryService.getAllContestTracks(any())).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/tracks", 1))
                .andExpect(status().isOk())
                .andDo(document("get-all-contest-track",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "대회 분과 목록"),
                                numberFieldWithPath("[].trackId", "분과 ID"),
                                stringFieldWithPath("[].trackName", "분과 이름"),
                                dateTimeFieldWithPath("[].updatedAt", "수정 일시")
                        )
                ));
    }
}
