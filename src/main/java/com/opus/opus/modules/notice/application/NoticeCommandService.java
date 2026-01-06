package com.opus.opus.modules.notice.application;

import static com.opus.opus.modules.notice.exception.NoticeExceptionType.NOT_FOUND_NOTICE;

import com.opus.opus.modules.notice.application.dto.NoticeConvenience;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
import com.opus.opus.modules.notice.domain.Notice;
import com.opus.opus.modules.notice.domain.dao.NoticeRepository;
import com.opus.opus.modules.notice.exception.NoticeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;

    private final NoticeConvenience noticeConvenience;

    public void createNotice(final NoticeRequest request) {
        noticeRepository.save(Notice.builder()
                .title(request.title())
                .description(request.description())
                .build());
    }

    public void updateNotice(final NoticeRequest request, final Long noticeId) {
        final Notice notice = noticeConvenience.getValidateExistNotice(noticeId);
        notice.updateNotice(request.title(), request.description());
    }

    public void deleteNotice(final Long noticeId) {
        noticeRepository.delete(noticeConvenience.getValidateExistNotice(noticeId));
    }

    public void createContestNotice(final Long contestId, final NoticeRequest request) {
        noticeRepository.save(Notice.builder()
                .contestId(contestId)
                .title(request.title())
                .description(request.description())
                .build());
    }

    public void updateContestNotice(final NoticeRequest request, final Long contestId, final Long noticeId) {
        final Notice notice =getContestNotice(contestId, noticeId);
        notice.updateNotice(request.title(), request.description());
    }

    private Notice getContestNotice(final Long contestId, final Long noticeId) {
        return noticeRepository.findByContestIdAndId(contestId, noticeId)
                .orElseThrow(() -> new NoticeException(NOT_FOUND_NOTICE));
    }
}
