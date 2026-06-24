package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NO_SUBMISSIONS_TO_ARCHIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionArchiveCommandService;
import com.opus.opus.modules.contest.application.dto.request.ArchiveRequest;
import com.opus.opus.modules.contest.application.dto.request.ArchiveTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.SubmissionArchive;
import com.opus.opus.modules.contest.domain.Contest;
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

public class ContestSubmissionArchiveCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionArchiveCommandService archiveService;

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
    private ContestTrack track;
    private ContestSubmissionItem item;
    private Team team;

    @BeforeEach
    void setUp() {
        when(fileStorage.loadAsStream(any())).thenAnswer(invocation -> new ByteArrayInputStream("data".getBytes()));

        contest = contestRepository.save(ContestFixture.createContest());
        track = trackRepository.save(ContestTrackFixture.createTrack(contest));
        item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        team = teamRepository.save(buildTeam(track.getId(), "AI팀1"));

        final var submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item));
        fileDocumentRepository.save(buildFileDocument(submission.getId(), "발표자료.pdf", 0));
        fileDocumentRepository.save(buildFileDocument(submission.getId(), "보고서.pdf", 1));
    }

    @Test
    @DisplayName("[성공] 선택한 대상의 제출 파일을 팀별 폴더 구조로 zip에 담는다.")
    void 선택한_대상의_제출_파일을_zip에_담는다() throws Exception {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), null)));

        final SubmissionArchive archive = archiveService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        assertThat(archive.fileName()).endsWith(".zip");
        assertThat(readEntryNames(out.toByteArray()))
                .containsExactlyInAnyOrder("AI팀1/발표자료.pdf", "AI팀1/보고서.pdf");
    }

    @Test
    @DisplayName("[실패] 대상에 해당하는 제출이 없으면 예외가 발생한다.")
    void 대상에_해당하는_제출이_없으면_예외가_발생한다() {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), 99999L)));

        assertThatThrownBy(() -> archiveService.generateArchive(contest.getId(), request))
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

    private FileDocument buildFileDocument(final Long submissionId, final String name, final int order) {
        return FileDocument.builder()
                .file(File.create(name, "files/2026-06-24/" + name, "application/pdf", 1000L))
                .submissionId(submissionId)
                .fileOrder(order)
                .build();
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
