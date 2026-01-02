package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class TeamApiDocsTest extends RestDocsTest {

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = "mock_access_token";
    }

    @Test
    @DisplayName("[성공] 팀의 포스터 이미지를 조회한다.")
    void 팀의_포스터_이미지를_조회한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final byte[] mockImageContent = "test-image-content".getBytes();
        final ImageResponse response = new ImageResponse(new ByteArrayResource(mockImageContent), "image/png");

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
    @DisplayName("[성공] 팀의 포스터 이미지를 등록한다.")
    void 팀의_포스터_이미지를_등록한다() throws Exception {
        // Given
        final Long teamId = 1L;
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "poster.png",
                MediaType.IMAGE_PNG_VALUE,
                "<<image-data>>".getBytes()
        );

        doNothing().when(teamCommandService).savePosterImage(any(), any());

        // When & Then
        mockMvc.perform(multipart("/teams/{teamId}/image/posters", teamId)
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("save-team-poster",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀장, 관리자, 팀원 권한)")
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
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-poster",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀장, 관리자, 팀원 권한)")
                        )
                ));
    }
}
