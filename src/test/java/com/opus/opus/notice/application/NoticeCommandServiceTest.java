package com.opus.opus.notice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
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

    private Notice notice;

    @BeforeEach
    void setUp() {
        notice = noticeRepository.save(NoticeFixture.createNotice());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 생성된다.")
    void 전체_공지사항이_정상적으로_생성된다() {
        final NoticeRequest request = new NoticeRequest("공지 제목", "공지 내용");

        noticeCommandService.createNotice(request);

        final Notice notice = noticeRepository.findAllByOrderByCreatedAtDesc().get(0);
        assertThat(notice.getTitle()).isEqualTo(request.title());
        assertThat(notice.getDescription()).isEqualTo(request.description());
        assertThat(notice.getContestId()).isNull();
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 수정된다.")
    void 전체_공지사항이_정상적으로_수정된다() {
        final NoticeRequest request = new NoticeRequest("수정 공지 제목", "수정 공지 내용");
        assertThat(notice.getTitle()).isEqualTo(notice.getTitle());

        noticeCommandService.updateNotice(request, notice.getId());

        final Notice updateNotice = noticeRepository.findAllByOrderByCreatedAtDesc().get(0);
        assertThat(updateNotice.getTitle()).isEqualTo(request.title());
        assertThat(updateNotice.getDescription()).isEqualTo(request.description());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항이 정상적으로 삭제된다.")
    void 전체_공지사항이_정상적으로_삭제된다() {
        assertThat(noticeRepository.count()).isEqualTo(1);

        noticeCommandService.deleteNotice(notice.getId());

        assertThat(noticeRepository.count()).isEqualTo(0);
    }
}
