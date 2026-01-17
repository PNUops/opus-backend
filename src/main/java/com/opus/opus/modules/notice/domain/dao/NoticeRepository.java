package com.opus.opus.modules.notice.domain.dao;

import com.opus.opus.modules.notice.domain.Notice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByContestIdIsNullOrderByCreatedAtDesc();

    List<Notice> findAllByContestIdOrderByCreatedAtDesc(final Long contestId);

    Optional<Notice> findByContestIdAndId(final Long contestId, final Long id);

    Optional<Notice> findByIdAndContestIdIsNull(final Long id);
}
