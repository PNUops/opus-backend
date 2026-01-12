package com.opus.opus.notice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
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

    private Notice notice;

    @BeforeEach
    void setUp() {
        notice = noticeRepository.save(NoticeFixture.createGlobalNotice());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항을 상세 조회할 수 있다.")
    void 전체_공지사항을_상세_조회할_수_있다() {
        final NoticeDetailResponse response = noticeQueryService.getNotice(notice.getId());

        assertThat(response.title()).isEqualTo(notice.getTitle());
        assertThat(response.description()).isEqualTo(notice.getDescription());
    }

    @Test
    @DisplayName("[성공] 전체 공지사항 목록을 조회할 수 있다.")
    void 전체_공지사항_목록을_조회할_수_있다() {
        final Notice anotherNotice = noticeRepository.save(NoticeFixture.createGlobalNotice());

        final List<NoticeSummaryResponse> responses = noticeQueryService.getAllNotices();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(NoticeSummaryResponse::noticeId)
                .containsExactlyInAnyOrder(notice.getId(), anotherNotice.getId());
    }
}
