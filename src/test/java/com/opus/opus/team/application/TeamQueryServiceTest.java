package com.opus.opus.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.ArrayList;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class TeamQueryServiceTest extends IntegrationTest {

    @Autowired
    private TeamQueryService teamQueryService;

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
    @DisplayName("[성공] 팀 포스터 이미지를 조회한다.")
    void 팀_포스터_이미지를_조회한다() {
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

        Resource resource = new ByteArrayResource("content".getBytes());
        given(fileStorageUtil.findFileAndType(savedFile.getId()))
                .willReturn(new Pair<>(resource, "image/webp"));

        // when
        ImageResponse response = teamQueryService.getPosterImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.contentType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("[실패] 팀이 존재하지 않으면 포스터 이미지를 조회할 수 없다.")
    void 팀이_존재하지_않으면_포스터_이미지를_조회할_수_없다() {
        // given
        long notExistTeamId = 999L;

        // when & then
        assertThatThrownBy(() -> teamQueryService.getPosterImage(notExistTeamId))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀 포스터 이미지가 존재하지 않으면 조회할 수 없다.")
    void 팀_포스터_이미지가_존재하지_않으면_조회할_수_없다() {
        // when & then
        assertThatThrownBy(() -> teamQueryService.getPosterImage(team.getId()))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_EXISTS_MATCHING_IMAGE_ID.errorMessage());
    }
}
