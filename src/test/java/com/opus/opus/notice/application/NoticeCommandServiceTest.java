package com.opus.opus.notice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.notice.application.NoticeCommandService;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
import com.opus.opus.modules.notice.domain.Notice;
import com.opus.opus.modules.notice.domain.dao.NoticeRepository;
import com.opus.opus.notice.NoticeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NoticeCommandServiceTest extends IntegrationTest {

    @Autowired
    private NoticeCommandService noticeCommandService;

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
        contest = contestRepository.save(ContestFixture.createContest(contestCategory.getId()));
        globalNotice = noticeRepository.save(NoticeFixture.createGlobalNotice());
        contestNotice = noticeRepository.save(NoticeFixture.createContestNotice(contest.getId()));
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 생성된다.")
    void 전체_공지사항이_정상적으로_생성된다() {
        final NoticeRequest request = new NoticeRequest("전체 공지 제목", "전체 공지 내용");

        noticeCommandService.createNotice(request);

        final Notice notice = noticeRepository.findAllByContestIdIsNullOrderByCreatedAtDesc().get(0);
        assertThat(notice.getTitle()).isEqualTo(request.title());
        assertThat(notice.getDescription()).isEqualTo(request.description());
        assertThat(notice.getContestId()).isNull();
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 수정된다.")
    void 전체_공지사항이_정상적으로_수정된다() {
        final String beforeTitle = globalNotice.getTitle();
        final String beforeDescription = globalNotice.getDescription();
        final NoticeRequest request = new NoticeRequest("수정 전체 공지 제목", "수정 전체 공지 내용");

        noticeCommandService.updateNotice(request, globalNotice.getId());

        final Notice updateNotice = noticeRepository.findAllByContestIdIsNullOrderByCreatedAtDesc().get(0);
        assertThat(updateNotice.getTitle()).isNotEqualTo(beforeTitle);
        assertThat(updateNotice.getDescription()).isNotEqualTo(beforeDescription);
        assertThat(updateNotice.getTitle()).isEqualTo(request.title());
        assertThat(updateNotice.getDescription()).isEqualTo(request.description());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 삭제된다.")
    void 전체_공지사항이_정상적으로_삭제된다() {
        assertThat(noticeRepository.count()).isEqualTo(2);

        noticeCommandService.deleteNotice(globalNotice.getId());

        assertThat(noticeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 대회별 공지사항이 정상적으로 생성된다.")
    void 대회별_공지사항이_정상적으로_생성된다() {
        final NoticeRequest request = new NoticeRequest("대회별 공지 제목", "대회별 공지 내용");

        noticeCommandService.createContestNotice(contestNotice.getContestId(), request);

        final Notice notice = noticeRepository.findAllByContestIdOrderByCreatedAtDesc(contestNotice.getContestId())
                .get(0);
        assertThat(notice.getTitle()).isEqualTo(request.title());
        assertThat(notice.getDescription()).isEqualTo(request.description());
    }

    @Test
    @DisplayName("[성공] 대회별 공지사항이 정상적으로 수정된다.")
    void 대회별_공지사항이_정상적으로_수정된다() {
        final String beforeTitle = contestNotice.getTitle();
        final String beforeDescription = contestNotice.getDescription();
        final NoticeRequest request = new NoticeRequest("수정 대회별 공지 제목", "수정 대회별 공지 내용");

        noticeCommandService.updateContestNotice(request, contestNotice.getContestId(), contestNotice.getId());

        final Notice updateNotice = noticeRepository.findAllByContestIdOrderByCreatedAtDesc(
                contestNotice.getContestId()).get(0);
        assertThat(updateNotice.getTitle()).isNotEqualTo(beforeTitle);
        assertThat(updateNotice.getDescription()).isNotEqualTo(beforeDescription);
        assertThat(updateNotice.getTitle()).isEqualTo(request.title());
        assertThat(updateNotice.getDescription()).isEqualTo(request.description());
    }
}
