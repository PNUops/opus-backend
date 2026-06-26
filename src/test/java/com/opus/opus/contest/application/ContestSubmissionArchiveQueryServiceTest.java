package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NO_SUBMISSIONS_TO_ARCHIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionArchiveQueryService;
import com.opus.opus.modules.contest.application.dto.request.ArchiveRequest;
import com.opus.opus.modules.contest.application.dto.request.ArchiveTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionArchive;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean
    private FileStorage fileStorage;

    private Contest contest;
    private ContestTrack trackAi;
    private ContestTrack trackWeb;
    private ContestSubmissionItem item;
    private Team teamAi1;
    private Team teamAi2;
    private Team teamWeb1;

    @BeforeEach
    void setUp() {
        when(fileStorage.loadAsStream(any())).thenAnswer(invocation -> new ByteArrayInputStream("data".getBytes()));

        contest = contestRepository.save(ContestFixture.createContest());
        trackAi = trackRepository.save(ContestTrackFixture.createTrack(contest));
        trackWeb = trackRepository.save(ContestTrackFixture.createTrack(contest));
        item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));

        teamAi1 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀1"));
        teamAi2 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀2"));
        teamWeb1 = teamRepository.save(buildTeam(trackWeb.getId(), "웹팀1"));

        final ContestSubmission submissionAi1 = saveSubmission(teamAi1.getId());
        saveFile(submissionAi1.getId(), "발표자료.pdf", 100L, 0);
        saveFile(submissionAi1.getId(), "보고서.pdf", 200L, 1);

        final ContestSubmission submissionAi2 = saveSubmission(teamAi2.getId());
        saveFile(submissionAi2.getId(), "발표자료.pdf", 300L, 0);

        final ContestSubmission submissionWeb1 = saveSubmission(teamWeb1.getId());
        saveFile(submissionWeb1.getId(), "발표자료.pdf", 400L, 0);
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

    @Test
    @DisplayName("[성공] 선택한 대상의 제출 파일을 팀별 폴더 구조로 zip에 담는다.")
    void 선택한_대상의_제출_파일을_zip에_담는다() throws Exception {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), null)));

        final SubmissionArchive archive = archiveQueryService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        assertThat(archive.fileName()).endsWith(".zip");
        assertThat(readEntryNames(out.toByteArray()))
                .containsExactlyInAnyOrder(
                        "AI팀1/발표자료.pdf", "AI팀1/보고서.pdf", "AI팀2/발표자료.pdf", "웹팀1/발표자료.pdf");
    }

    @Test
    @DisplayName("[성공] 같은 폴더에 동일 파일명이 있으면 엔트리명을 디듀프해 zip이 깨지지 않는다.")
    void 동일_파일명이면_엔트리명을_디듀프한다() throws Exception {
        // teamId에 매칭되는 Team이 없어 모두 unknown/ 폴더로 떨어지는 고아 제출물
        final ContestSubmission orphan1 = saveSubmission(88888L);
        saveFile(orphan1.getId(), "발표자료.pdf", 100L, 0);
        final ContestSubmission orphan2 = saveSubmission(99999L);
        saveFile(orphan2.getId(), "발표자료.pdf", 100L, 0);

        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), null)));

        final SubmissionArchive archive = archiveQueryService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        final List<String> names = readEntryNames(out.toByteArray());
        assertThat(names).contains("unknown/발표자료.pdf", "unknown/발표자료(1).pdf");
        assertThat(names).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("[실패] 대상에 해당하는 제출이 없으면 예외가 발생한다.")
    void 대상에_해당하는_제출이_없으면_예외가_발생한다() {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), 99999L)));

        assertThatThrownBy(() -> archiveQueryService.generateArchive(contest.getId(), request))
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NO_SUBMISSIONS_TO_ARCHIVE.errorMessage());
    }

    private List<String> readEntryNames(final byte[] zipBytes) throws Exception {
        final List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        return names;
    }

    private ContestSubmission saveSubmission(final Long teamId) {
        return submissionRepository.save(ContestSubmissionFixture.createSubmission(teamId, item));
    }

    private void saveFile(final Long submissionId, final String name, final Long size, final int order) {
        fileDocumentRepository.save(FileDocument.builder()
                .file(File.create(name, "files/2026-06-24/" + submissionId + "-" + order + ".pdf",
                        "application/pdf", size))
                .submissionId(submissionId)
                .fileOrder(order)
                .build());
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
