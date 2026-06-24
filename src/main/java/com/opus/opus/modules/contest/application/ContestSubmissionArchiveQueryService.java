package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetsResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.ArchiveFileEntry;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionArchiveQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final TeamConvenience teamConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public ArchiveTargetsResponse getArchiveTargets(final Long contestId, final Long submissionTypeId,
                                                    final Long trackId) {
        contestConvenience.validateExistContest(contestId);

        final List<ContestSubmission> submissions = contestSubmissionConvenience.getSubmissionsOfContest(contestId)
                .stream()
                .filter(submission -> submissionTypeId == null
                        || submission.getSubmissionItem().getId().equals(submissionTypeId))
                .toList();

        final Map<Long, Long> teamTrackMap = teamConvenience.getTeamsOfContest(contestId).stream()
                .filter(team -> team.getTrackId() != null)
                .collect(toMap(Team::getId, Team::getTrackId));
        final Map<Long, String> trackNameMap = contestTrackConvenience.getValidateExistTracks(contestId).stream()
                .collect(toMap(ContestTrack::getId, ContestTrack::getTrackName));
        final Map<Long, Long> sizeBySubmission = fileDocumentQueryService.getArchiveEntries(
                        submissions.stream().map(ContestSubmission::getId).toList()).stream()
                .collect(groupingBy(ArchiveFileEntry::submissionId, summingLong(ArchiveFileEntry::fileSize)));

        final Map<GroupKey, GroupData> groups = new LinkedHashMap<>();
        for (final ContestSubmission submission : submissions) {
            final Long teamTrackId = teamTrackMap.get(submission.getTeamId());
            if (teamTrackId == null || (trackId != null && !trackId.equals(teamTrackId))) {
                continue;
            }
            final GroupKey key = new GroupKey(submission.getSubmissionItem().getId(), teamTrackId);
            final GroupData data = groups.computeIfAbsent(key,
                    ignored -> new GroupData(submission.getSubmissionItem().getName()));
            data.teamIds.add(submission.getTeamId());
            data.estimatedSize += sizeBySubmission.getOrDefault(submission.getId(), 0L);
        }

        final List<ArchiveTargetResponse> archives = groups.entrySet().stream()
                .map(entry -> new ArchiveTargetResponse(
                        entry.getKey().submissionTypeId(),
                        entry.getValue().submissionTypeName,
                        entry.getKey().trackId(),
                        trackNameMap.get(entry.getKey().trackId()),
                        entry.getValue().teamIds.size(),
                        entry.getValue().estimatedSize))
                .toList();

        return new ArchiveTargetsResponse(archives);
    }

    private record GroupKey(Long submissionTypeId, Long trackId) {
    }

    private static final class GroupData {
        private final String submissionTypeName;
        private final Set<Long> teamIds = new HashSet<>();
        private long estimatedSize = 0L;

        private GroupData(final String submissionTypeName) {
            this.submissionTypeName = submissionTypeName;
        }
    }
}
