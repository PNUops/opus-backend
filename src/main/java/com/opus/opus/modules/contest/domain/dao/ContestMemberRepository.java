package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestMemberRepository extends JpaRepository<ContestMember, Long> {

}
