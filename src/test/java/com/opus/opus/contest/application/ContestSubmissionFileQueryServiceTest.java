package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NO_SUBMISSIONS_TO_ARCHIVE;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionFileQueryService;
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
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
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

public class ContestSubmissionFileQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionFileQueryService contestSubmissionFileQueryService;

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
    private ContestSubmission submissionAi1;
    private FileDocument fileAi1;

    @BeforeEach
    void setUp() {
        when(fileStorage.load(any())).thenReturn("doc".getBytes());
        when(fileStorage.loadAsStream(any())).thenAnswer(invocation -> new ByteArrayInputStream("data".getBytes()));

        contest = contestRepository.save(ContestFixture.createContest());
        trackAi = trackRepository.save(ContestTrackFixture.createTrack(contest));
        trackWeb = trackRepository.save(ContestTrackFixture.createTrack(contest));
        item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));

        final Team teamAi1 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀1"));
        final Team teamAi2 = teamRepository.save(buildTeam(trackAi.getId(), "AI팀2"));
        final Team teamWeb1 = teamRepository.save(buildTeam(trackWeb.getId(), "웹팀1"));

        submissionAi1 = saveSubmission(teamAi1.getId());
        fileAi1 = saveFile(submissionAi1.getId(), "발표자료.pdf", 100L, 0);
        saveFile(submissionAi1.getId(), "보고서.pdf", 200L, 1);

        final ContestSubmission submissionAi2 = saveSubmission(teamAi2.getId());
        saveFile(submissionAi2.getId(), "발표자료.pdf", 300L, 0);

        final ContestSubmission submissionWeb1 = saveSubmission(teamWeb1.getId());
        saveFile(submissionWeb1.getId(), "발표자료.pdf", 400L, 0);
    }

    @Test
    @DisplayName("[성공] 제출 파일을 개별 다운로드한다.")
    void 제출_파일을_개별_다운로드한다() {
        final DocumentFileDownload result = contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), submissionAi1.getId(), fileAi1.getId());

        assertThat(result.fileName()).isEqualTo("발표자료.pdf");
        assertThat(result.submissionId()).isEqualTo(submissionAi1.getId());
    }

    @Test
    @DisplayName("[실패] 파일이 해당 제출물에 속하지 않으면 예외가 발생한다.")
    void 파일이_해당_제출물에_속하지_않으면_예외가_발생한다() {
        final ContestSubmission other = saveSubmission(submissionAi1.getTeamId());

        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), other.getId(), fileAi1.getId()))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_FOUND.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물의 파일은 다운로드할 수 없다.")
    void 존재하지_않는_제출물의_파일은_다운로드할_수_없다() {
        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), 99999L, fileAi1.getId()))
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 파일은 다운로드할 수 없다.")
    void 존재하지_않는_대회의_파일은_다운로드할_수_없다() {
        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                99999L, submissionAi1.getId(), fileAi1.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출물 종류 × 분과 조합별로 제출 팀 수와 예상 용량을 집계한다.")
    void 종류_분과_조합별로_집계한다() {
        final ArchiveTargetsResponse response = contestSubmissionFileQueryService.getArchiveTargets(
                contest.getId(), null, null);

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
        final ArchiveTargetsResponse response = contestSubmissionFileQueryService.getArchiveTargets(
                contest.getId(), null, trackWeb.getId());

        assertThat(response.archives())
                .extracting(ArchiveTargetResponse::trackId, ArchiveTargetResponse::submittedTeamCount)
                .containsExactly(tuple(trackWeb.getId(), 1));
    }

    @Test
    @DisplayName("[성공] 제출물 종류로 필터링하면 해당 종류 조합만 반환한다.")
    void 제출물_종류로_필터링한다() {
        final ContestSubmissionItem otherItem = submissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final Team otherTeam = teamRepository.save(buildTeam(trackAi.getId(), "기타팀"));
        saveFile(saveSubmission(otherTeam.getId(), otherItem).getId(), "기타.pdf", 50L, 0);

        final ArchiveTargetsResponse response = contestSubmissionFileQueryService.getArchiveTargets(
                contest.getId(), item.getId(), null);

        assertThat(response.archives())
                .extracting(ArchiveTargetResponse::submissionTypeId)
                .containsOnly(item.getId());
    }

    @Test
    @DisplayName("[성공] 선택한 대상의 제출 파일을 팀별 폴더 구조로 zip에 담는다.")
    void 선택한_대상의_제출_파일을_zip에_담는다() throws Exception {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), null)));

        final SubmissionArchive archive = contestSubmissionFileQueryService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        assertThat(archive.fileName()).endsWith(".zip");
        assertThat(readEntryNames(out.toByteArray()))
                .containsExactlyInAnyOrder(
                        "AI팀1/발표자료.pdf", "AI팀1/보고서.pdf", "AI팀2/발표자료.pdf", "웹팀1/발표자료.pdf");
    }

    @Test
    @DisplayName("[성공] 특정 분과를 지정하면 해당 분과 팀의 파일만 zip에 담는다.")
    void 특정_분과만_zip에_담는다() throws Exception {
        final ArchiveRequest request = new ArchiveRequest(
                List.of(new ArchiveTargetRequest(item.getId(), trackWeb.getId())));

        final SubmissionArchive archive = contestSubmissionFileQueryService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        assertThat(readEntryNames(out.toByteArray())).containsExactly("웹팀1/발표자료.pdf");
    }

    @Test
    @DisplayName("[성공] 같은 팀이 서로 다른 제출 항목에 동일 파일명을 내면 엔트리명을 디듀프한다.")
    void 동일_파일명이면_엔트리명을_디듀프한다() throws Exception {
        // 제출 항목별로 폴더를 나누지 않으므로(팀명/파일명) 같은 팀의 항목 간 파일명이 충돌할 수 있다.
        final ContestSubmissionItem otherItem = submissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final Team team = teamRepository.save(buildTeam(trackAi.getId(), "중복팀"));
        saveFile(saveSubmission(team.getId(), item).getId(), "자료.pdf", 100L, 0);
        saveFile(saveSubmission(team.getId(), otherItem).getId(), "자료.pdf", 200L, 0);

        final ArchiveRequest request = new ArchiveRequest(List.of(
                new ArchiveTargetRequest(item.getId(), null),
                new ArchiveTargetRequest(otherItem.getId(), null)));

        final SubmissionArchive archive = contestSubmissionFileQueryService.generateArchive(contest.getId(), request);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        archive.body().writeTo(out);

        final List<String> names = readEntryNames(out.toByteArray());
        assertThat(names).contains("중복팀/자료.pdf", "중복팀/자료(1).pdf");
        assertThat(names).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("[실패] 대상에 해당하는 제출이 없으면 예외가 발생한다.")
    void 대상에_해당하는_제출이_없으면_예외가_발생한다() {
        final ArchiveRequest request = new ArchiveRequest(List.of(new ArchiveTargetRequest(item.getId(), 99999L)));

        assertThatThrownBy(() -> contestSubmissionFileQueryService.generateArchive(contest.getId(), request))
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
        return saveSubmission(teamId, item);
    }

    private ContestSubmission saveSubmission(final Long teamId, final ContestSubmissionItem submissionItem) {
        return submissionRepository.save(ContestSubmissionFixture.createSubmission(teamId, submissionItem));
    }

    private FileDocument saveFile(final Long submissionId, final String name, final Long size, final int order) {
        return fileDocumentRepository.save(FileDocument.builder()
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
