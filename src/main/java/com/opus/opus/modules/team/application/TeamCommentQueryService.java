package com.opus.opus.modules.team.application;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.response.TeamCommentResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamCommentQueryService {

    private final TeamCommentRepository teamCommentRepository;

    private final MemberConvenience memberConvenience;
    private final TeamConvenience teamConvenience;

    public List<TeamCommentResponse> getComments(final Long teamId) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final List<TeamComment> comments = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId());

        final List<Long> memberIds = comments.stream()
                .map(TeamComment::getMemberId)
                .distinct()
                .toList();

        final Map<Long, String> memberIdNameMap = memberConvenience.findAllById(memberIds)
                .stream()
                .collect(toMap(Member::getId, Member::getName));

        return comments.stream()
                .map(comment -> new TeamCommentResponse(
                        comment.getId(),
                        comment.getDescription(),
                        comment.getMemberId(),
                        memberIdNameMap.get(comment.getMemberId()),
                        team.getId()
                ))
                .toList();
    }
}

