package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.file.application.convenience.FileDocumentConvenience;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionItemRepository contestSubmissionItemRepository;
    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final FileDocumentConvenience fileDocumentConvenience;

    public ContestSubmissionDetailResponse getSubmissionDetail(final Long contestId, final Long submissionId,
                                                               final Member member) {
        contestConvenience.validateExistContest(contestId);

        final ContestSubmission submission = contestSubmissionConvenience.getValidateSubmissionInContest(contestId, submissionId);
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();

        final Team team = teamConvenience.getValidateTeamInContest(submission.getTeamId(), contestId);
        // 학생은 해당 팀의 팀장/팀원만 조회할 수 있다. (관리자/교수/직원/외부멘토는 제한 없음)
        teamMemberConvenience.validateTeamMemberIfStudent(team.getId(), member);

        final String trackName = submissionItem.getContestTrack() != null
                ? submissionItem.getContestTrack().getTrackName()
                : null;
        final List<FileDocument> fileDocuments = fileDocumentConvenience.findAllBySubmissionId(submissionId);

        /* TODO
        제출물 코멘트 기능이 아직 없어서, 제출물 카운트를 0으로 고정하였습니다.
        코멘트 기능 추가 시 실제 개수로 교체가 필요합니다.
         */
        final int commentCount = 0;

        return ContestSubmissionDetailResponse.of(submission, team, trackName, fileDocuments, commentCount);
    }

    public ContestSubmissionSummaryResponse getSubmissionSummary(final Long contestId, final Long teamId,
                                                                  final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        final long totalItemCount = contestSubmissionItemRepository.countByContestId(contestId);
        final List<ContestSubmission> submissions = contestSubmissionRepository.findAllByTeamIdAndContestId(teamId,
                contestId);
        final long submittedCount = submissions.size();
        final List<Long> submissionIds = submissions.stream().map(ContestSubmission::getId).toList();
        final long totalFeedbackCount = submissionIds.isEmpty() ? 0
                : contestSubmissionFeedbackRepository.countBySubmissionIdIn(submissionIds);

        return new ContestSubmissionSummaryResponse(totalItemCount, submittedCount, totalFeedbackCount);
    }

    public List<ContestSubmissionTimelineResponse> getSubmissionTimeline(final Long contestId, final Long teamId,
                                                                          final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        return contestSubmissionRepository.findAllByTeamIdAndContestId(teamId, contestId)
                .stream()
                .map(ContestSubmissionTimelineResponse::from)
                .toList();
    }
}
