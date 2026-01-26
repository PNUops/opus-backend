package com.opus.opus.notice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.notice.application.NoticeQueryService;
import com.opus.opus.modules.notice.application.dto.response.NoticeDetailResponse;
import com.opus.opus.modules.notice.application.dto.response.NoticeSummaryResponse;
import com.opus.opus.modules.notice.domain.Notice;
import com.opus.opus.modules.notice.domain.dao.NoticeRepository;
import com.opus.opus.notice.NoticeFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NoticeQueryServiceTest extends IntegrationTest {

    @Autowired
    private NoticeQueryService noticeQueryService;

    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;

    private Contest contest;
    private ContestCategory contestCategory;
    private Notice globalNotice;
    private Notice contestNotice;

    @BeforeEach
    void setUp() {
        contestCategory = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(contestCategory.getId()));
        globalNotice = noticeRepository.save(NoticeFixture.createGlobalNotice());
        contestNotice = noticeRepository.save(NoticeFixture.createContestNotice(contest.getId()));
    }

    @Test
    @DisplayName("[성공] 전체 공지사항을 상세 조회할 수 있다.")
    void 전체_공지사항을_상세_조회할_수_있다() {
        final NoticeDetailResponse response = noticeQueryService.getNotice(globalNotice.getId());

        assertThat(response.title()).isEqualTo(globalNotice.getTitle());
        assertThat(response.description()).isEqualTo(globalNotice.getDescription());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항 목록을 조회할 수 있다.")
    void 전체_공지사항_목록을_조회할_수_있다() {
        final Notice anotherNotice = noticeRepository.save(NoticeFixture.createGlobalNotice());

        final List<NoticeSummaryResponse> responses = noticeQueryService.getAllNotices();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(NoticeSummaryResponse::noticeId)
                .containsExactlyInAnyOrder(globalNotice.getId(), anotherNotice.getId());
    }

    @Test
    @DisplayName("[성공] 대회별 공지사항을 상세 조회할 수 있다.")
    void 대회별_공지사항을_상세_조회할_수_있다() {
        final NoticeDetailResponse response = noticeQueryService.getContestNotice(contestNotice.getContestId(),
                contestNotice.getId());

        assertThat(response.title()).isEqualTo(contestNotice.getTitle());
        assertThat(response.description()).isEqualTo(contestNotice.getDescription());
    }

    @Test
    @DisplayName("[성공] 대회별 공지사항 목록을 조회할 수 있다.")
    void 대회별_공지사항_목록을_조회할_수_있다() {
        final Notice anotherContestNotice = noticeRepository.save(NoticeFixture.createContestNotice(contest.getId()));

        final List<NoticeSummaryResponse> responses = noticeQueryService.getAllContestNotices(
                anotherContestNotice.getContestId());

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(NoticeSummaryResponse::noticeId)
                .containsExactlyInAnyOrder(contestNotice.getId(), anotherContestNotice.getId());
    }
}
