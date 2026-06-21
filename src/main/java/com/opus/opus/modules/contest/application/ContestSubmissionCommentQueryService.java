package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionCommentResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionCommentRepository;
import com.opus.opus.modules.file.application.convenience.FileCommentConvenience;
import com.opus.opus.modules.file.application.dto.CommentFileInfo;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionCommentQueryService {

    private final ContestSubmissionCommentRepository contestSubmissionCommentRepository;

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final MemberConvenience memberConvenience;
    private final FileCommentConvenience fileCommentConvenience;

    public List<ContestSubmissionCommentResponse> getComments(final Long contestId, final Long submissionId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final List<ContestSubmissionComment> comments = contestSubmissionCommentRepository.findAllBySubmissionIdOrderByIdDesc(submissionId);

        final Map<Long, Member> memberMap = memberConvenience.findAllById(comments.stream()
                        .map(ContestSubmissionComment::getMemberId)
                        .distinct()
                        .toList())
                .stream()
                .collect(toMap(Member::getId, member -> member));

        final Map<Long, List<CommentFileInfo>> filesByCommentId = fileCommentConvenience.findFilesGroupedByCommentIds(
                comments.stream()
                        .map(ContestSubmissionComment::getId)
                        .toList());

        return comments.stream()
                .map(comment -> ContestSubmissionCommentResponse.of(comment, memberMap.get(comment.getMemberId()),
                        filesByCommentId.getOrDefault(comment.getId(), List.of())))
                .toList();
    }
}
