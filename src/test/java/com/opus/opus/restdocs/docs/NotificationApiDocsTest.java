package com.opus.opus.restdocs.docs;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.dto.response.NotificationResponse;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class NotificationApiDocsTest extends RestDocsTest {

    private Member loginMember;
    private static final String MEMBER_TOKEN = "Bearer member.access.token";

    @BeforeEach
    void setUp() {
        this.loginMember = MemberFixture.createMember();
        setField(loginMember, "id", 1L);
        when(memberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(loginMember);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 알림 목록을 조회할 수 있다.")
    void 유효한_요청이면_정상적으로_알림_목록을_조회할_수_있다() throws Exception {
        final List<NotificationResponse> responses = List.of(
                new NotificationResponse(1L, "팀 알림 제목입니다.", "팀 알림 내용입니다.", NotificationType.TEAM, 1L,
                        "/teams/1", false, now()),
                new NotificationResponse(2L, "팀 댓글 알림 제목입니다.", "팀 댓글 알림 내용입니다.", NotificationType.TEAM_COMMENT, 1L,
                        "/teams/1", false, now())
        );

        when(notificationQueryService.getNotifications(any())).thenReturn(responses);

        mockMvc.perform(get("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-notifications",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (회원)")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "알림 목록"),
                                numberFieldWithPath("[].id", "알림 ID"),
                                stringFieldWithPath("[].title", "알림 제목"),
                                stringFieldWithPath("[].content", "알림 내용"),
                                stringFieldWithPath("[].targetType", "알림 타입 (TEAM, TEAM_COMMENT, TEAM_AWARDS)"),
                                numberFieldWithPath("[].targetId", "알림 대상 ID"),
                                stringFieldWithPath("[].redirectUrl", "이동 URL"),
                                booleanFieldWithPath("[].isRead", "읽음 여부"),
                                dateTimeFieldWithPath("[].createdAt", "알림 생성 시각")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 단일 알림이 읽음 처리된다.")
    void 유효한_요청이면_정상적으로_단일_알림이_읽음_처리된다() throws Exception {
        doNothing().when(notificationCommandService).updateSingleNotification(any(), any());

        mockMvc.perform(patch("/notifications/{notificationId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("update-single-notification",
                        pathParameters(
                                parameterWithName("notificationId").description("알림 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (회원)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 전체 알림이 읽음 처리된다.")
    void 유효한_요청이면_정상적으로_전체_알림이_읽음_처리된다() throws Exception {
        doNothing().when(notificationCommandService).updateNotificationAll(any());

        mockMvc.perform(patch("/notifications")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("update-all-notifications",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (회원)")
                        )
                ));
    }
}
