package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamLikeConvenience {

    private final TeamLikeRepository teamLikeRepository;

    public Map<Long, Boolean> getLikeMap(final List<Team> teams, final Member member) {
        return (member != null) ? teamLikeRepository.findAllByMemberIdAndTeamIn(member.getId(), teams).stream()
                .collect(toMap(tl -> tl.getTeam().getId(), TeamLike::getIsLiked))
                : Collections.emptyMap();
    }
}
