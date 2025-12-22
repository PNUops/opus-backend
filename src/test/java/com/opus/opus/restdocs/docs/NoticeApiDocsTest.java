package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
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
}
