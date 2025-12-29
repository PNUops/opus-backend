package com.opus.opus.modules.notice.application.dto;

import static com.opus.opus.modules.notice.exception.NoticeExceptionType.NOT_FOUND_NOTICE;

import com.opus.opus.modules.notice.domain.Notice;
import com.opus.opus.modules.notice.domain.dao.NoticeRepository;
import com.opus.opus.modules.notice.exception.NoticeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeConvenience {

    private final NoticeRepository noticeRepository;

    public Notice getValidateExistNotice(final Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() -> new NoticeException(NOT_FOUND_NOTICE));
    }
}

