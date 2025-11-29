package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestTrack;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestTrackRepository extends JpaRepository<ContestTrack, Long> {
    boolean existsByContestIdAndTrackName(final Long contestId, final String trackName);

    Optional<ContestTrack> findByIdAndContestId(Long trackId, Long contestId);
}
