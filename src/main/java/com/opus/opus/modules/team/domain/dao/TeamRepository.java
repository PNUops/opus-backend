package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestId(Long contestId);

    List<Team> findByContestId(Long contestId);

    List<Team> findAllByContestId(Long contestId);

    @Query("select coalesce(max(t.itemOrder), 0) from Team t where t.contestId = :contestId")
    Integer findMaxItemOrderByContestId(final Long contestId);

    boolean existsByTrackId(final Long trackId);

}
