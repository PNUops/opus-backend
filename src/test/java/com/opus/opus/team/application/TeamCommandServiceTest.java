package com.opus.opus.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class TeamCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamCommandService teamCommandService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FileRepository fileRepository;

    @MockitoBean
    private FileStorageUtil fileStorageUtil;

    private Team team;

    @BeforeEach
    void setUp() {
        team = teamRepository.save(Team.builder()
                .teamName("팀 이름")
                .projectName("프로젝트 이름")
                .contestId(1L)
                .trackId(1L)
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지를 저장한다.")
    void 팀_포스터_이미지를_저장한다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());

        // when
        teamCommandService.savePosterImage(team.getId(), image);

        // then
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(team.getId()), eq(TEAM), eq(POSTER));
    }

    @Test
    @DisplayName("[실패] 팀이 존재하지 않으면 포스터 이미지를 저장할 수 없다.")
    void 팀이_존재하지_않으면_포스터_이미지를_저장할_수_없다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());
        final long notExistTeamId = 999L;

        // when & then
        assertThatThrownBy(() -> teamCommandService.savePosterImage(notExistTeamId, image))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지를 삭제한다.")
    void 팀_포스터_이미지를_삭제한다() {
        // given
        final File file = File.builder()
                .referenceId(team.getId())
                .referenceType(TEAM)
                .imageType(POSTER)
                .filePath("path/to/poster.webp")
                .name("poster.jpg")
                .build();
        final File savedFile = fileRepository.save(file);
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        // when
        teamCommandService.deletePosterImage(team.getId());

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지가 없어도 삭제 요청 시 예외가 발생하지 않는다.")
    void 팀_포스터_이미지가_없어도_삭제_요청_시_예외가_발생하지_않는다() {
        // when
        teamCommandService.deletePosterImage(team.getId());

        // then
        verify(fileStorageUtil, never()).deleteFile(any());
    }
}
