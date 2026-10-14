package com.opus.opus.contest.api;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// 투표 리포트 API는 GET /contests/** 가 permitAll이라 @Secured("ROLE_관리자")만으로 막는다.
// 보안 필터 체인(JWT 필터 → 메서드 보안 → 401/403 핸들러)을 실제로 거치도록 springSecurity()를 적용해 검증한다.
public class ContestVoteReportAuthorizationTest extends IntegrationTest {

    private static final String VOTE_REPORT_URL = "/contests/{contestId}/votes/report";
    private static final String VOTE_REPORT_PDF_URL = "/contests/{contestId}/votes/report/pdf";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;

    private MockMvc securedMockMvc;
    private Contest contest;
    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setUpSecurity() {
        securedMockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        final Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        contest = contestRepository.save(newContest);

        adminToken = createAccessToken(
                memberRepository.save(MemberFixture.createMemberWithEmailAndRole("admin@pusan.ac.kr", ROLE_관리자)));
        studentToken = createAccessToken(
                memberRepository.save(MemberFixture.createMemberWithEmailAndRole("student@pusan.ac.kr", ROLE_학생)));
    }

    @Test
    @DisplayName("[성공] 관리자는 투표 리포트를 조회할 수 있다.")
    void 관리자는_투표_리포트를_조회할_수_있다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_URL, contest.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 관리자가 아니면 투표 리포트 조회 시 403 에러를 반환한다.")
    void 관리자가_아니면_투표_리포트_조회_시_403_에러를_반환한다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_URL, contest.getId())
                        .header(HttpHeaders.AUTHORIZATION, studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[실패] 로그인하지 않으면 투표 리포트 조회 시 401 에러를 반환한다.")
    void 로그인하지_않으면_투표_리포트_조회_시_401_에러를_반환한다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_URL, contest.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[성공] 관리자는 투표 결과 PDF를 다운로드할 수 있다.")
    void 관리자는_투표_결과_PDF를_다운로드할_수_있다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_PDF_URL, contest.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 관리자가 아니면 투표 결과 PDF 다운로드 시 403 에러를 반환한다.")
    void 관리자가_아니면_투표_결과_PDF_다운로드_시_403_에러를_반환한다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_PDF_URL, contest.getId())
                        .header(HttpHeaders.AUTHORIZATION, studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[실패] 로그인하지 않으면 투표 결과 PDF 다운로드 시 401 에러를 반환한다.")
    void 로그인하지_않으면_투표_결과_PDF_다운로드_시_401_에러를_반환한다() throws Exception {
        securedMockMvc.perform(get(VOTE_REPORT_PDF_URL, contest.getId()))
                .andExpect(status().isUnauthorized());
    }

    private String createAccessToken(final Member member) {
        final List<String> roles = member.getRoles().stream()
                .map(MemberRoleType::name)
                .toList();
        return "Bearer " + jwtProvider.createToken(String.valueOf(member.getId()), roles, member.getName());
    }
}
