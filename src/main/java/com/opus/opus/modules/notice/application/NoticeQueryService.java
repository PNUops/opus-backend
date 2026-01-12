package com.opus.opus.modules.notice.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.notice.application.convenience.NoticeConvenience;
import com.opus.opus.modules.notice.application.dto.response.NoticeDetailResponse;
import com.opus.opus.modules.notice.application.dto.response.NoticeSummaryResponse;
import com.opus.opus.modules.notice.domain.Notice;
import com.opus.opus.modules.notice.domain.dao.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    private final NoticeConvenience noticeConvenience;
    private final ContestConvenience contestConvenience;

    public NoticeDetailResponse getNotice(final Long noticeId) {
        final Notice notice = noticeConvenience.getValidateExistNotice(noticeId);
        return NoticeDetailResponse.from(notice);
    }

    public List<NoticeSummaryResponse> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(NoticeSummaryResponse::from)
                .toList();
    }

    public NoticeDetailResponse getContestNotice(final Long contestId, final Long noticeId) {
        final Notice notice = noticeConvenience.getValidateContestNotice(contestId, noticeId);
        return NoticeDetailResponse.from(notice);
    }

    public List<NoticeSummaryResponse> getAllContestNotices(final Long contestId) {
        contestConvenience.validateExistContest(contestId);
        return noticeRepository.findAllByContestIdOrderByCreatedAtDesc(contestId)
                .stream()
                .map(NoticeSummaryResponse::from)
                .toList();
    }
}
