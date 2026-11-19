package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.AdminActivityHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActivityHistoryRepository extends JpaRepository<AdminActivityHistory, Long> {

    List<AdminActivityHistory> findTop4ByOrderByCreatedAtDescIdDesc();
}
