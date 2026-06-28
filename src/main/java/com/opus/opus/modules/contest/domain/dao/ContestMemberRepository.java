package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestMemberRepository extends JpaRepository<ContestMember, Long> {

    List<ContestMember> findAllByContestId(final Long contestId);

    boolean existsByContestIdAndMemberId(final Long contestId, final Long memberId);

    Optional<ContestMember> findByIdAndContestId(final Long id, final Long contestId);
}
