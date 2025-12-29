package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByContestId(Long contestId);

    boolean existsByTrackId(final Long trackId);

    @Query("SELECT MAX(t.itemOrder) FROM Team t WHERE t.contestId = :contestId")
    Optional<Integer> findMaxItemOrderByContestId(@Param("contestId") Long contestId);
}
