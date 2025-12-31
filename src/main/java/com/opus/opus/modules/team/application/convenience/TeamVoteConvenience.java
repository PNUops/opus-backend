package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamVoteConvenience {

    private final TeamVoteRepository teamVoteRepository;

    public Map<Long, Boolean> getVoteMap(final List<Team> teams, final Member member) {
        return (member != null) ? teamVoteRepository.findAllByMemberIdAndTeamIn(member.getId(), teams).stream()
                .collect(toMap(tv -> tv.getTeam().getId(), TeamVote::getIsVoted))
                : Collections.emptyMap();
    }
}
