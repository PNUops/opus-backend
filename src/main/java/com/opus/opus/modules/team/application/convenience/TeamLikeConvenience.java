package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamLikeConvenience {

    private final TeamLikeRepository teamLikeRepository;

    public Map<Long, Boolean> getLikeMap(final Long contestId, final Member member) {
        return teamLikeRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .collect(toMap(tl -> tl.getTeam().getId(), TeamLike::getIsLiked));
    }
}
