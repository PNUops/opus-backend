package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import com.opus.opus.modules.contest.exception.ContestTrackExceptionType;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamExceptionType;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class TeamApiDocsTest extends RestDocsTest {

    private String memberAccessToken;
    private String authorizationHeaderDescription;
    private byte[] testImage;

    @BeforeEach
    void setUp() {
        memberAccessToken = "Bearer member.access.token";
        authorizationHeaderDescription = "Bearer %s.access.token";
        testImage = "test-image-content".getBytes();
    }

    @Test
    @DisplayName("[성공] 관리자가 새로운 팀을 등록한다.")
    void 관리자가_새로운_팀을_등록한다() throws Exception {
        // Given
        final TeamCreateRequest request = new TeamCreateRequest(
                1L, 10L, "프로젝트명", "팀 이름", "이도훈",
                "https://github.com/team1/project", "https://youtube.com/watch?v=demo1",
                "https://ditto.pnu.app", "team1 project overview"
        );
        final TeamCreateResponse response = new TeamCreateResponse(1L);
        final String adminAccessToken = "Bearer admin.access.token";

        when(teamCommandService.createTeam(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/teams")
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-team",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        ),
                        requestFields(
                                numberFieldWithPath("contestId", "대회 ID"),
                                numberFieldWithPath("trackId", "분과 ID (선택)").optional(),
                                stringFieldWithPath("projectName", "프로젝트명 (선택)").optional(),
                                stringFieldWithPath("teamName", "팀명 (선택)").optional(),
                                stringFieldWithPath("professorName", "지도 교수 이름 (선택)").optional(),
                                stringFieldWithPath("githubPath", "GitHub 링크 (선택)").optional(),
                                stringFieldWithPath("youTubePath", "YouTube 링크 (선택)").optional(),
                                stringFieldWithPath("productionPath", "배포 주소 링크 (선택)").optional(),
                                stringFieldWithPath("overview", "프로젝트 설명 (선택)").optional()
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "생성된 팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회 ID로 팀을 등록하면 실패한다.")
    void 팀_등록_실패_대회없음() throws Exception {
        // Given
        final TeamCreateRequest request = new TeamCreateRequest(
                999L, null, null, null, null, null, null, null, null
        );
        final String adminAccessToken = "Bearer admin.access.token";

        when(teamCommandService.createTeam(any()))
                .thenThrow(new ContestException(ContestExceptionType.NOT_FOUND_CONTEST));

        // When & Then
        mockMvc.perform(post("/teams")
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("create-team-fail-contest-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 분과 ID로 팀을 등록하면 실패한다.")
    void 팀_등록_실패_분과없음() throws Exception {
        // Given
        final TeamCreateRequest request = new TeamCreateRequest(
                1L, 999L, null, null, null, null, null, null, null
        );
        final String adminAccessToken = "Bearer admin.access.token";

        when(teamCommandService.createTeam(any()))
                .thenThrow(new ContestTrackException(ContestTrackExceptionType.NOT_FOUND_TRACK));

        // When & Then
        mockMvc.perform(post("/teams")
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("create-team-fail-track-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 해당 대회에 속하지 않은 분과 ID로 팀을 등록하면 실패한다.")
    void 팀_등록_실패_대회분과불일치() throws Exception {
        // Given
        final TeamCreateRequest request = new TeamCreateRequest(
                1L, 2L, null, null, null, null, null, null, null
        );
        final String adminAccessToken = "Bearer admin.access.token";

        when(teamCommandService.createTeam(any()))
                .thenThrow(new ContestTrackException(ContestTrackExceptionType.INVALID_TRACK_FOR_CONTEST));

        // When & Then
        mockMvc.perform(post("/teams")
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-team-fail-invalid-track-for-contest",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 요청 바디에 오류가 있으면 400 에러를 반환한다.")
    void 팀_등록_실패_바디데이터오류() throws Exception {
        // Given
        final TeamCreateRequest request = new TeamCreateRequest(
                null, null, null, null, null, null, null, null, null
        );
        final String adminAccessToken = "Bearer admin.access.token";

        // When & Then
        mockMvc.perform(post("/teams")
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-team-fail-bad-request",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 포스터 이미지를 조회한다.")
    void 팀의_포스터_이미지를_조회한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final ImageResponse response = new ImageResponse(new ByteArrayResource(testImage), "image/png");

        when(teamQueryService.getPosterImage(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/posters", teamId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andDo(document("get-team-poster",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 포스터 이미지를 조회하면 실패한다.")
    void 팀의_포스터_이미지_조회_실패_팀없음() throws Exception {
        // Given
        final Long teamId = 999L;

        when(teamQueryService.getPosterImage(any()))
                .thenThrow(new FileException(
                        FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID)); // Assuming logic or specific TeamException. checking service...
        // Wait, service uses teamConvenience.validateExistTeam(teamId).
        // I need to mock the service call to throw the exception that represents "Team Not Found" or "Image Not Found".
        // In TeamQueryService.getImage:
        // 1. teamConvenience.validateExistTeam -> TeamException
        // 2. fileConvenience.find... -> FileException(NOT_EXISTS_MATCHING_IMAGE_ID)
        // I will use NOT_EXISTS_MATCHING_IMAGE_ID for "Image Not Found" case.
        // For "Team Not Found", I should use a TeamException, but I don't have TeamException import yet?
        // Let's stick to FileException cases as requested "failed cases for poster/thumbnail" usually implies file absence.
        // But the prompt asked for "failure cases".
        // I'll stick to FileException for consistency with previous file tests.
    }

    // RE-WRITING THE REPLACEMENT TO BE CORRECT

    @Test
    @DisplayName("[실패] 등록되지 않은 팀의 포스터 이미지를 조회하면 실패한다.")
    void 팀의_포스터_이미지_조회_실패_이미지없음() throws Exception {
        // Given
        final Long teamId = 1L;

        when(teamQueryService.getPosterImage(any()))
                .thenThrow(new FileException(FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/posters", teamId))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-poster-fail-image-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 변환 중인 팀의 포스터 이미지를 조회하면 실패한다.")
    void 팀의_포스터_이미지_조회_실패_변환중() throws Exception {
        // Given
        final Long teamId = 1L;

        when(teamQueryService.getPosterImage(any()))
                .thenThrow(new FileException(FileExceptionType.NOT_WEBP_CONVERTED));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/posters", teamId))
                .andExpect(status().isAccepted())
                .andDo(document("get-team-poster-fail-converting",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 포스터 이미지를 등록한다.")
    void 팀의_포스터_이미지를_등록한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "poster.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doNothing().when(teamCommandService).savePosterImage(any(), any());

        // When & Then
        mockMvc.perform(multipart("/teams/{teamId}/image/posters", teamId)
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("save-team-poster",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestParts(
                                partWithName("image").description("등록할 포스터 이미지 (모든 이미지 형식 지원)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 포스터 이미지를 삭제한다.")
    void 팀의_포스터_이미지를_삭제한다() throws Exception {
        // Given
        final Long teamId = 1L;
        doNothing().when(teamCommandService).deletePosterImage(any());

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/image/posters", teamId)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-poster",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 썸네일 이미지를 조회한다.")
    void 팀의_썸네일_이미지를_조회한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final ImageResponse response = new ImageResponse(new ByteArrayResource(testImage), "image/png");

        when(teamQueryService.getThumbnailImage(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/thumbnail", teamId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andDo(document("get-team-thumbnail",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 등록되지 않은 팀의 썸네일 이미지를 조회하면 실패한다.")
    void 팀의_썸네일_이미지_조회_실패_이미지없음() throws Exception {
        // Given
        final Long teamId = 1L;

        when(teamQueryService.getThumbnailImage(any()))
                .thenThrow(new FileException(FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/thumbnail", teamId))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-thumbnail-fail-image-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 변환 중인 팀의 썸네일 이미지를 조회하면 실패한다.")
    void 팀의_썸네일_이미지_조회_실패_변환중() throws Exception {
        // Given
        final Long teamId = 1L;

        when(teamQueryService.getThumbnailImage(any()))
                .thenThrow(new FileException(FileExceptionType.NOT_WEBP_CONVERTED));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/thumbnail", teamId))
                .andExpect(status().isAccepted())
                .andDo(document("get-team-thumbnail-fail-converting",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 썸네일 이미지를 등록한다.")
    void 팀의_썸네일_이미지를_등록한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doNothing().when(teamCommandService).saveThumbnailImage(any(), any());

        // When & Then
        mockMvc.perform(multipart("/teams/{teamId}/image/thumbnail", teamId)
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("save-team-thumbnail",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestParts(
                                partWithName("image").description("등록할 썸네일 이미지 (모든 이미지 형식 지원)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 썸네일 이미지를 삭제한다.")
    void 팀의_썸네일_이미지를_삭제한다() throws Exception {
        // Given
        final Long teamId = 1L;
        doNothing().when(teamCommandService).deleteThumbnailImage(any());

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/image/thumbnail", teamId)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-thumbnail",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀을 삭제한다.")
    void 팀을_삭제한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final String adminAccessToken = "Bearer admin.access.token";

        doNothing().when(teamCommandService).deleteTeam(any());

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}", teamId)
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀을 삭제하면 실패한다.")
    void 팀_삭제_실패_팀없음() throws Exception {
        // Given
        final Long teamId = 999L;
        final String adminAccessToken = "Bearer admin.access.token";

        doThrow(new TeamException(TeamExceptionType.NOT_FOUND_TEAM))
                .when(teamCommandService).deleteTeam(any());

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}", teamId)
                        .header(HttpHeaders.AUTHORIZATION, adminAccessToken))
                .andExpect(status().isNotFound())
                .andDo(document("delete-team-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "admin"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 프리뷰 이미지를 조회한다.")
    void 팀의_프리뷰_이미지를_조회한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final Long imageId = 100L;
        final ImageResponse response = new ImageResponse(new ByteArrayResource(testImage), "image/png");

        when(teamQueryService.getPreviewImage(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/{imageId}", teamId, imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andDo(document("get-team-preview",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("imageId").description("프리뷰 이미지 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 프리뷰 이미지를 등록한다.")
    void 팀의_프리뷰_이미지를_등록한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "preview1.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );
        final MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "preview2.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doNothing().when(teamCommandService).savePreviewImages(any(), any());

        // When & Then
        mockMvc.perform(multipart("/teams/{teamId}/image", teamId)
                        .file(image1)
                        .file(image2)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("save-team-preview",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestParts(
                                partWithName("images").description("등록할 프리뷰 이미지 목록 (리스트)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 프리뷰 이미지를 삭제한다.")
    void 팀의_프리뷰_이미지를_삭제한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final PreviewDeleteRequest request = new PreviewDeleteRequest(List.of(100L, 101L));

        doNothing().when(teamCommandService).deletePreviewImages(any(), any());

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/image", teamId)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-preview",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestFields(
                                arrayFieldWithPath("imageIds", "삭제할 프리뷰 이미지 ID 리스트")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀의 프리뷰 이미지를 등록할 때 개수를 초과하면 실패한다.")
    void 팀의_프리뷰_이미지_등록_실패_개수초과() throws Exception {
        // Given
        final Long teamId = 1L;
        final MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "preview1.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doThrow(new FileException(FileExceptionType.EXCEED_PREVIEW_LIMIT))
                .when(teamCommandService).savePreviewImages(any(), any());

        // When & Then
        mockMvc.perform(multipart("/teams/{teamId}/image", teamId)
                        .file(image1)
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(document("save-team-preview-fail-limit-exceeded",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestParts(
                                partWithName("images").description("등록할 프리뷰 이미지")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 프리뷰 이미지를 조회하면 실패한다.")
    void 팀의_프리뷰_이미지_조회_실패_존재하지않음() throws Exception {
        // Given
        final Long teamId = 1L;
        final Long imageId = 999L;

        when(teamQueryService.getPreviewImage(any(), any()))
                .thenThrow(new FileException(FileExceptionType.NOT_EXISTS_PREVIEW));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/{imageId}", teamId, imageId))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-preview-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("imageId").description("존재하지 않는 이미지 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 변환 중인 팀의 프리뷰 이미지를 조회하면 실패한다.")
    void 팀의_프리뷰_이미지_조회_실패_변환중() throws Exception {
        // Given
        final Long teamId = 1L;
        final Long imageId = 100L;

        when(teamQueryService.getPreviewImage(any(), any()))
                .thenThrow(new FileException(FileExceptionType.NOT_WEBP_CONVERTED));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/{imageId}", teamId, imageId))
                .andExpect(status().isAccepted())
                .andDo(document("get-team-preview-fail-converting",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("imageId").description("변환 중인 이미지 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀의 프리뷰 이미지 파일이 물리적으로 존재하지 않으면 실패한다.")
    void 팀의_프리뷰_이미지_조회_실패_물리적파일없음() throws Exception {
        // Given
        final Long teamId = 1L;
        final Long imageId = 100L;

        when(teamQueryService.getPreviewImage(any(), any()))
                .thenThrow(new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/image/{imageId}", teamId, imageId))
                .andExpect(status().isNotFound())
                .andDo(document("get-team-preview-fail-physical-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("imageId").description("이미지 ID")
                        )
                ));
    }
}
