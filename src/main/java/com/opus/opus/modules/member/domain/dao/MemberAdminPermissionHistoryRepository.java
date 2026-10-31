package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.MemberAdminPermissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAdminPermissionHistoryRepository extends JpaRepository<MemberAdminPermissionHistory, Long> {
}
