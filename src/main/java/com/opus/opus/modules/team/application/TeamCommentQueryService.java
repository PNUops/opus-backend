package com.opus.opus.modules.team.application;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
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
    private final TeamMemberConvenience teamMemberConvenience;

    public List<TeamCommentResponse> getComments(final Long teamId, final Member member) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final boolean isTeamOnlyCommentViewer = isTeamOnlyCommentViewer(team.getId(), member);

        final List<TeamComment> comments = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId())
                .stream()
                .filter(comment -> comment.isPublic() || isTeamOnlyCommentViewer || comment.isMine(member.getId()))
                .toList();

        final List<Long> memberIds = comments.stream()
                .map(TeamComment::getMemberId)
                .distinct()
                .toList();

        final Map<Long, Member> memberMap = memberConvenience.getMembersByIds(memberIds);

        return comments.stream()
                .map(comment -> TeamCommentResponse.of(comment, memberMap.get(comment.getMemberId())))
                .toList();
    }

    private boolean isTeamOnlyCommentViewer(final Long teamId, final Member member) {
        return member.isAdmin() || teamMemberConvenience.isTeamMember(teamId, member.getId());
    }
}
