package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestTrack;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestTrackRepository extends JpaRepository<ContestTrack, Long> {
    boolean existsByContestIdAndTrackName(final Long contestId, final String trackName);

    List<ContestTrack> findAllByContestId(final Long contestId);
}
