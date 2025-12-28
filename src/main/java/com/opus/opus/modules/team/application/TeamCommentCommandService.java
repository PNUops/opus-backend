package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;

import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.exception.TeamCommentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamCommentCommandService {

    private final TeamCommentRepository teamCommentRepository;

    private final TeamConvenience teamConvenience;

    public void createComment(final Long teamId, final Long memberId, final String description) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);

        teamCommentRepository.save(TeamComment.builder()
                .description(description)
                .memberId(memberId)
                .team(team)
                .build());
    }

    public void updateComment(final Long teamId, final Long commentId, final Long memberId, final String newDescription) {
        teamConvenience.getValidateExistTeam(teamId);
        final TeamComment comment = getValidateExistComment(commentId);
        isMine(comment, memberId);

        comment.updateDescription(newDescription);
    }

    public void deleteComment(final Long teamId, final Long commentId, final Long memberId) {
        teamConvenience.validateExistTeam(teamId);
        final TeamComment comment = getValidateExistComment(commentId);
        isMine(comment, memberId);

        teamCommentRepository.delete(comment);
    }

    private void isMine(final TeamComment comment, final Long memberId) {
        if (!comment.isMine(memberId)) {
            throw new TeamCommentException(NOT_OWNER_COMMENT);
        }
    }

    private TeamComment getValidateExistComment(final Long commentId) {
        return teamCommentRepository.findById(commentId).orElseThrow(() -> new TeamCommentException(NOT_FOUND_COMMENT));
    }
}

