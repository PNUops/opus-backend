package com.opus.opus.modules.notice.domain.dao;

import com.opus.opus.modules.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
