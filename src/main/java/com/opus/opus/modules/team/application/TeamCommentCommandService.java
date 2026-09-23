package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.COMMENT_NOT_BELONG_TO_TEAM;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.event.TeamCommentNotificationEvent;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.TeamCommentVisibility;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.exception.TeamCommentException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamCommentCommandService {

    private final TeamCommentRepository teamCommentRepository;

    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ApplicationEventPublisher eventPublisher;

    public void createComment(final Long teamId, final Member member, final String description,
                              final TeamCommentVisibility visibility) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final TeamComment comment = TeamComment.builder()
                .description(description)
                .memberId(member.getId())
                .team(team)
                .visibility(visibility)
                .build();
        validateTeamOnlyCommentWriter(comment, member);

        teamCommentRepository.save(comment);

        final List<Long> memberIds = teamMemberConvenience.findRealMemberIdsByTeamId(teamId)
                .stream()
                .filter(id -> !id.equals(member.getId()))
                .toList();
        final String teamDisplayName = team.getTeamName() != null ? team.getTeamName() : team.getProjectName();
        eventPublisher.publishEvent(new TeamCommentNotificationEvent(memberIds, teamId, teamDisplayName));
    }

    public void updateComment(final Long teamId, final Long commentId, final Long memberId, final String newDescription) {
        teamConvenience.validateExistTeam(teamId);
        final TeamComment comment = getValidateExistComment(commentId);

        validateCommentBelongsToTeam(comment, teamId);
        isMine(comment, memberId);

        comment.updateDescription(newDescription);
    }

    public void deleteComment(final Long teamId, final Long commentId, final Long memberId) {
        teamConvenience.validateExistTeam(teamId);
        final TeamComment comment = getValidateExistComment(commentId);

        validateCommentBelongsToTeam(comment, teamId);
        isMine(comment, memberId);

        teamCommentRepository.delete(comment);
    }

    private void validateTeamOnlyCommentWriter(final TeamComment comment, final Member member) {
        if (!comment.isPublic() && !member.hasStaffRole()) {
            throw new TeamCommentException(NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT);
        }
    }

    private void isMine(final TeamComment comment, final Long memberId) {
        if (!comment.isMine(memberId)) {
            throw new TeamCommentException(NOT_OWNER_COMMENT);
        }
    }

    private TeamComment getValidateExistComment(final Long commentId) {
        return teamCommentRepository.findById(commentId).orElseThrow(() -> new TeamCommentException(NOT_FOUND_COMMENT));
    }

    private void validateCommentBelongsToTeam(final TeamComment comment, final Long teamId) {
        if (!comment.getTeam().getId().equals(teamId)) {
            throw new TeamCommentException(COMMENT_NOT_BELONG_TO_TEAM);
        }
    }
}
