package com.opus.opus.contest.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionArchiveQueryService;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetsResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionArchiveQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionArchiveQueryService archiveQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository trackRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    private Contest contest;
    private ContestTrack trackAi;
    private ContestTrack trackWeb;
    private ContestSubmissionItem item;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        trackAi = trackRepository.save(ContestTrackFixture.createTrack(contest));
        trackWeb = trackRepository.save(ContestTrackFixture.createTrack(contest));
        item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));

        final Team team1 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀1"));
        final Team team2 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀2"));
        final Team team3 = teamRepository.save(buildTeam(trackWeb.getId(), "웹팀1"));

        saveSubmissionWithFiles(team1.getId(), 100L, 200L);
        saveSubmissionWithFiles(team2.getId(), 300L);
        saveSubmissionWithFiles(team3.getId(), 400L);
    }

    @Test
    @DisplayName("[성공] 제출물 종류 × 분과 조합별로 제출 팀 수와 예상 용량을 집계한다.")
    void 종류_분과_조합별로_집계한다() {
        final ArchiveTargetsResponse response = archiveQueryService.getArchiveTargets(contest.getId(), null, null);

        assertThat(response.archives())
                .extracting(ArchiveTargetResponse::trackId, ArchiveTargetResponse::submittedTeamCount,
                        ArchiveTargetResponse::estimatedSize)
                .containsExactlyInAnyOrder(
                        tuple(trackAi.getId(), 2, 600L),
                        tuple(trackWeb.getId(), 1, 400L)
                );
    }

    @Test
    @DisplayName("[성공] 분과로 필터링하면 해당 분과 조합만 반환한다.")
    void 분과로_필터링한다() {
        final ArchiveTargetsResponse response =
                archiveQueryService.getArchiveTargets(contest.getId(), null, trackWeb.getId());

        assertThat(response.archives())
                .extracting(ArchiveTargetResponse::trackId, ArchiveTargetResponse::submittedTeamCount)
                .containsExactly(tuple(trackWeb.getId(), 1));
    }

    private void saveSubmissionWithFiles(final Long teamId, final Long... fileSizes) {
        final var submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(teamId, item));
        int order = 0;
        for (final Long size : fileSizes) {
            fileDocumentRepository.save(FileDocument.builder()
                    .file(File.create("file.pdf", "files/2026-06-24/" + teamId + "-" + order + ".pdf",
                            "application/pdf", size))
                    .submissionId(submission.getId())
                    .fileOrder(order++)
                    .build());
        }
    }

    private Team buildTeam(final Long trackId, final String teamName) {
        return Team.builder()
                .teamName(teamName)
                .projectName("프로젝트")
                .contestId(contest.getId())
                .trackId(trackId)
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .teamAwards(new ArrayList<>())
                .build();
    }
}
