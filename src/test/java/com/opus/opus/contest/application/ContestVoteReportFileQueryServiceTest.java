package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_NOT_ENDED;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndTeamName;
import static com.opus.opus.team.TeamVoteFixture.createTeamVote;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestVoteReportFileQueryService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.time.LocalDateTime;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class ContestVoteReportFileQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestVoteReportFileQueryService contestVoteReportFileQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Contest contest;

    @BeforeEach
    void setUp() {
        final Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        newContest.updateMaxVotesLimit(2);
        contest = contestRepository.save(newContest);
    }

    @Test
    @DisplayName("[성공] 투표가 마감된 대회의 투표 결과를 PDF로 내보낼 수 있다.")
    void 투표가_마감된_대회의_투표_결과를_PDF로_내보낼_수_있다() throws Exception {
        final Team team = teamRepository.save(createTeamWithContestIdAndTeamName(contest.getId(), "스마트화분팀"));
        teamVoteRepository.save(createTeamVote(team, 101L));
        teamVoteRepository.save(createTeamVote(team, 102L));

        final FileDownload download = contestVoteReportFileQueryService.generateVoteReportPdf(contest.getId());

        assertThat(download.fileName()).startsWith("제1회테스트대회_투표결과_").endsWith(".pdf");
        assertThat(download.mimeType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        try (PDDocument document = Loader.loadPDF(download.resource().getContentAsByteArray())) {
            final String text = new PDFTextStripper().getText(document);
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(2);
            assertThat(text).contains("제 1회 테스트 대회 온라인 투표 결과", "스마트화분팀");
        }
    }

    @Test
    @DisplayName("[성공] 투표가 없는 대회도 투표 결과를 PDF로 내보낼 수 있다.")
    void 투표가_없는_대회도_투표_결과를_PDF로_내보낼_수_있다() throws Exception {
        final FileDownload download = contestVoteReportFileQueryService.generateVoteReportPdf(contest.getId());

        try (PDDocument document = Loader.loadPDF(download.resource().getContentAsByteArray())) {
            assertThat(new PDFTextStripper().getText(document)).contains("투표 없음");
        }
    }

    @Test
    @DisplayName("[성공] 최대 투표 수가 0인 대회는 투표권을 모두 사용한 투표자를 0명으로 표시한다.")
    void 최대_투표_수가_0인_대회는_투표권을_모두_사용한_투표자를_0명으로_표시한다() throws Exception {
        final Team team = teamRepository.save(createTeamWithContestIdAndTeamName(contest.getId(), "스마트화분팀"));
        teamVoteRepository.save(createTeamVote(team, 101L));
        contest.updateMaxVotesLimit(0);

        final FileDownload download = contestVoteReportFileQueryService.generateVoteReportPdf(contest.getId());

        try (PDDocument document = Loader.loadPDF(download.resource().getContentAsByteArray())) {
            assertThat(new PDFTextStripper().getText(document)).contains("0표 모두 사용 0명");
        }
    }

    @Test
    @DisplayName("[성공] 투표 결과 PDF에 투표자의 역할과 입학연도를 표시한다.")
    void 투표_결과_PDF에_투표자의_역할과_입학연도를_표시한다() throws Exception {
        final Member student = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202112345@pusan.ac.kr", ROLE_학생));
        final Team team = teamRepository.save(createTeamWithContestIdAndTeamName(contest.getId(), "스마트화분팀"));
        teamVoteRepository.save(createTeamVote(team, student.getId()));

        final FileDownload download = contestVoteReportFileQueryService.generateVoteReportPdf(contest.getId());

        try (PDDocument document = Loader.loadPDF(download.resource().getContentAsByteArray())) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("학생", "2021학번")
                    .doesNotContain("ROLE_", "알 수 없음(탈퇴 회원)", "학번 없음(교직원·탈퇴 등)");
        }
    }

    @Test
    @DisplayName("[실패] 투표가 마감되지 않은 대회의 투표 결과는 PDF로 내보낼 수 없다.")
    void 투표가_마감되지_않은_대회의_투표_결과는_PDF로_내보낼_수_없다() {
        contest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> contestVoteReportFileQueryService.generateVoteReportPdf(contest.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(VOTE_NOT_ENDED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 투표 결과는 PDF로 내보낼 수 없다.")
    void 존재하지_않는_대회의_투표_결과는_PDF로_내보낼_수_없다() {
        assertThatThrownBy(() -> contestVoteReportFileQueryService.generateVoteReportPdf(99999L))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }
}
