package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, Long> {
}
