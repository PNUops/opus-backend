package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFileResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectsResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse.FeedbackStatus;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class ContestMentorApiDocsTest extends RestDocsTest {

    private static final String MENTOR_TOKEN = "Bearer mentor.access.token";

    @BeforeEach
    void setUp() {
        final Member member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 멘토가 담당 프로젝트 목록을 조회한다.")
    void 멘토가_담당_프로젝트_목록을_조회한다() throws Exception {
        final MentorProjectsResponse response = new MentorProjectsResponse(2, 5L, List.of(
                new MentorProjectResponse(10L, "AI팀1", "AI 프로젝트", "융합트랙", "ROLE_교수", 5L),
                new MentorProjectResponse(11L, "보안팀2", "보안 프로젝트", "융합트랙", "ROLE_교수", 0L)
        ));

        when(contestMentorQueryService.getMentorProjects(any())).thenReturn(response);

        mockMvc.perform(get("/contests/mentors/me/projects")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-mentor-projects",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (교수/외부멘토)")
                        ),
                        responseFields(
                                numberFieldWithPath("assignedTeamCount", "담당 팀 수"),
                                numberFieldWithPath("pendingFeedbackCount", "전체 피드백 대기 건수"),
                                arrayFieldWithPath("projects", "담당 프로젝트 목록"),
                                numberFieldWithPath("projects[].teamId", "팀 ID"),
                                stringFieldWithPath("projects[].teamName", "팀명"),
                                stringFieldWithPath("projects[].projectName", "프로젝트명"),
                                stringFieldWithPath("projects[].trackName", "분과(트랙)명 (분과 미지정 시 null)"),
                                stringFieldWithPath("projects[].roleType", "역할 (MemberRoleType)"),
                                numberFieldWithPath("projects[].pendingFeedbackCount", "이 팀의 피드백 대기 건수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 멘토가 담당 팀의 제출물 및 피드백 상태를 조회한다.")
    void 멘토가_담당_팀의_제출물_및_피드백_상태를_조회한다() throws Exception {
        final TeamSubmissionsResponse response = new TeamSubmissionsResponse(10L, "AI팀1", "AI 프로젝트", "융합트랙", 1L,
                List.of(
                        new MentorSubmissionResponse(12L, 3L, "중간발표 자료2", FeedbackStatus.COMPLETED,
                                List.of(new ContestSubmissionFileResponse(201L, "AI프로젝트_AI팀1_중간발표자료2.pptx", 13002342L))),
                        new MentorSubmissionResponse(13L, 4L, "최종발표자료", FeedbackStatus.PENDING,
                                List.of(new ContestSubmissionFileResponse(205L, "AI프로젝트_AI팀1_최종발표자료.pptx", 13002342L)))
                ));

        when(contestMentorQueryService.getTeamSubmissions(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/teams/{teamId}/submissions", 1, 10)
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-mentor-team-submissions",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (교수/외부멘토)")
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "팀 ID"),
                                stringFieldWithPath("teamName", "팀명"),
                                stringFieldWithPath("projectName", "프로젝트명"),
                                stringFieldWithPath("trackName", "분과(트랙)명 (분과 미지정 시 null)"),
                                numberFieldWithPath("pendingFeedbackCount", "이 팀의 피드백 대기 건수"),
                                arrayFieldWithPath("submissions", "제출물 목록"),
                                numberFieldWithPath("submissions[].submissionId", "제출 ID"),
                                numberFieldWithPath("submissions[].submissionItemId", "제출 항목 ID"),
                                stringFieldWithPath("submissions[].submissionItemName", "제출 항목명"),
                                stringFieldWithPath("submissions[].feedbackStatus", "피드백 상태 (COMPLETED/PENDING)"),
                                arrayFieldWithPath("submissions[].files", "첨부 파일 목록"),
                                numberFieldWithPath("submissions[].files[].fileId", "첨부 파일 ID"),
                                stringFieldWithPath("submissions[].files[].fileName", "파일명"),
                                numberFieldWithPath("submissions[].files[].fileSize", "파일 크기 (byte)")
                        )
                ));
    }
}
