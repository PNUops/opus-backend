package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.MemberWithdrawalHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberWithdrawalHistoryRepository extends JpaRepository<MemberWithdrawalHistory, Long> {

    Optional<MemberWithdrawalHistory> findTopByMemberIdOrderByIdDesc(final Long memberId);
}
