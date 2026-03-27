package com.opus.opus.member.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.file.FileFixture;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class MemberQueryServiceTest extends IntegrationTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 가입된 회원은 이메일 찾기를 할 수 있다.")
    void 가입된_회원은_이메일_찾기를_할_수_있다() {
        final EmailFindResponse response = memberQueryService.getMyEmail(member.getStudentId());

        assertThat(response.email()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("[실패] 미가입 회원은 이메일 찾기가 불가하다.")
    void 미가입_회원은_이메일_찾기가_불가하다() {
        final String notExistMemberEmail = "qwqw@pusan.ac.kr";

        assertThatThrownBy(() -> {
            memberQueryService.getMyEmail(notExistMemberEmail);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 프로필 이미지가 있으면 이미지 응답이 반환된다.")
    void 프로필_이미지가_있으면_이미지_응답이_반환된다() {
        // given
        final File savedFile = fileRepository.save(FileFixture.createMemberProfileFile(member.getId()));
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        final Resource resource = new ByteArrayResource("content".getBytes());
        given(fileStorageUtil.findFileAndType(savedFile.getId()))
                .willReturn(new Pair<>(resource, "image/webp"));

        // when
        final ImageResponse response = memberQueryService.getProfileImage(member);

        // then
        assertThat(response).isNotNull();
        assertThat(response.contentType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("[실패] 프로필 이미지가 없으면 예외가 발생한다.")
    void 프로필_이미지가_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> memberQueryService.getProfileImage(member))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_EXISTS_MATCHING_IMAGE_ID.errorMessage());
    }

    @Test
    @DisplayName("[성공] 로그인한 회원은 계정 정보를 조회할 수 있다.")
    void 로그인한_회원은_계정_정보를_조회할_수_있다() {
        final AccountInfoResponse response = memberQueryService.getAccountInfo(member.getId());

        assertThat(response.name()).isEqualTo(member.getName());
        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.githubUrl()).isNull();
        assertThat(response.isProfilePublic()).isTrue();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 회원의 계정 정보는 조회할 수 없다.")
    void 존재하지_않는_회원의_계정_정보는_조회할_수_없다() {
        assertThatThrownBy(() -> {
            memberQueryService.getAccountInfo(999L);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }
}
