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
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description(String.format(authorizationHeaderDescription, "(teamLeader/admin/teamMember)"))
                        ),
                        requestFields(
                                arrayFieldWithPath("imageIds", "삭제할 프리뷰 이미지 ID 리스트")
                        )
                ));
    }
}
