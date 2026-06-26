package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestMemberRepository extends JpaRepository<ContestMember, Long> {

    List<ContestMember> findAllByContestId(final Long contestId);
}
