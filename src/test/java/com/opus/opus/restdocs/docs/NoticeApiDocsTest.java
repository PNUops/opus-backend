package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;
import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
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
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
import com.opus.opus.modules.notice.application.dto.response.NoticeDetailResponse;
import com.opus.opus.modules.notice.application.dto.response.NoticeSummaryResponse;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class NoticeApiDocsTest extends RestDocsTest {

    private Member admin;
    private Member member;
    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() {
        this.admin = MemberFixture.createMember();
        setField(admin, "id", 1L);
        adminToken = jwtProvider.createToken(String.valueOf(admin.getId()), List.of(ROLE_관리자.name()), admin.getName());

        this.member = MemberFixture.createMember();
        setField(member, "id", 2L);
        memberToken = jwtProvider.createToken(String.valueOf(member.getId()), List.of(ROLE_회원.name()),
                member.getName());
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 공지사항이 생성된다.")
    void 유효한_요청이면_정상적으로_전체_공지사항이_생성된다() throws Exception {
        final NoticeRequest request = new NoticeRequest("공지 제목", "공지 내용");

        doNothing().when(noticeCommandService).createNotice(any());

        mockMvc.perform(post("/notices")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-notice",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("title", "공지 제목"),
                                stringFieldWithPath("description", "공지 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 공지사항이 수정된다.")
    void 유효한_요청이면_정상적으로_전체_공지사항이_수정된다() throws Exception {
        final NoticeRequest request = new NoticeRequest("수정된 공지 제목", "수정된 공지 내용");

        doNothing().when(noticeCommandService).updateNotice(any(), any());

        mockMvc.perform(patch("/notices/{noticeId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-notice",
                        pathParameters(
                                parameterWithName("noticeId").description("공지 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("title", "수정된 공지 제목"),
                                stringFieldWithPath("description", "수정된 공지 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 공지사항이 삭제된다.")
    void 유효한_요청이면_정상적으로_전체_공지사항이_삭제된다() throws Exception {
        doNothing().when(noticeCommandService).deleteNotice(any());

        mockMvc.perform(delete("/notices/{noticeId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-notice",
                        pathParameters(
                                parameterWithName("noticeId").description("공지 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 공지사항 상세 조회를 할 수 있다.")
    void 유효한_요청이면_정상적으로_전체_공지사항_상세_조회를_할_수_있다() throws Exception {
        final NoticeDetailResponse response = new NoticeDetailResponse("공지 제목", "공지 내용", now(), now());

        when(noticeQueryService.getNotice(any())).thenReturn(response);

        mockMvc.perform(get("/notices/{noticeId}", 1L))
                .andExpect(status().isOk())
                .andDo(document("get-notice",
                        pathParameters(
                                parameterWithName("noticeId").description("공지 ID")
                        ),
                        responseFields(
                                stringFieldWithPath("title", "공지 제목"),
                                stringFieldWithPath("description", "공지 내용"),
                                dateTimeFieldWithPath("createdAt", "공지 생성 시각"),
                                dateTimeFieldWithPath("updatedAt", "공지 수정 시각")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 공지사항 목록을 조회할 수 있다.")
    void 유효한_요청이면_정상적으로_전체_공지사항_목록을_조회할_수_있다() throws Exception {
        final List<NoticeSummaryResponse> responses = List.of(
                new NoticeSummaryResponse(1L, "공지 제목 1", now()),
                new NoticeSummaryResponse(2L, "공지 제목 2", now())
        );

        when(noticeQueryService.getAllNotices()).thenReturn(responses);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andDo(document("get-all-notices",
                        responseFields(
                                arrayFieldWithPath("[]", "공지 목록"),
                                numberFieldWithPath("[].noticeId", "공지 ID"),
                                stringFieldWithPath("[].title", "공지 제목"),
                                dateTimeFieldWithPath("[].createdAt", "공지 생성 시각")
                        )
                ));
    }
}
