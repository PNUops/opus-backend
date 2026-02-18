package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamVoteConvenience {

    private final TeamVoteRepository teamVoteRepository;

    public List<TeamVote> getAllTeamVoteDesc(final List<Long> teamIds) {
        return teamVoteRepository.findAllByTeamIdInOrderByCreatedAtDesc(teamIds);
    }
}
