package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamLikeConvenience {

    private final TeamLikeRepository teamLikeRepository;

    public long countAllLikes() {
        return teamLikeRepository.countByIsLikedTrueAndTeamIsDeletedFalse();
    }
}
