package com.opus.opus.modules.notice.domain.dao;

import com.opus.opus.modules.notice.domain.Notice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedAtDesc();

    List<Notice> findAllByContestIdOrderByCreatedAtDesc(Long contestId);

    Optional<Notice> findByContestIdAndId(Long contestId, Long id);
}
