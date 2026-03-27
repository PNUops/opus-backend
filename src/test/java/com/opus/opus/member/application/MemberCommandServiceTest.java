package com.opus.opus.member.application;

import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.EMAIL_AUTH_LIMIT_EXCEEDED;
import static com.opus.opus.modules.member.exception.MemberExceptionType.GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_UPDATE_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.opus.opus.global.security.oauth2.GoogleOAuth2UserService;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.file.FileFixture;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.GithubUrlUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.request.ProfileVisibilityUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.StudentIdUpdateRequest;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import org.springframework.mock.web.MockMultipartFile;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberCommandServiceTest extends IntegrationTest {

    @Autowired
    private MemberCommandService memberCommandService;
    @Autowired
    private GoogleOAuth2UserService googleOAuth2UserService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FileRepository fileRepository;

    private Member teamLeader;
    private EmailAuthRequest emailAuthRequest;

    @BeforeEach
    void setUp() {
        teamLeader = memberRepository.save(MemberFixture.createMember());
        emailAuthRequest = new EmailAuthRequest("qwer1234@pusan.ac.kr");

        authRedisUtil.delete("email:auth:count:" + emailAuthRequest.email());
        authRedisUtil.delete("signup:email:auth:" + emailAuthRequest.email());
        authRedisUtil.delete("signup:email:verified:" + emailAuthRequest.email());
        authRedisUtil.delete("signin:email:auth:" + emailAuthRequest.email());
        authRedisUtil.delete("signin:email:verified:" + emailAuthRequest.email());
    }

    @Test
    @DisplayName("[성공] 회원가입 시 이메일 인증 코드가 정상 발급된다.")
    void 회원가입_시_이메일_인증_코드가_정상_발급된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);

        assertTrue(authRedisUtil.exists("signup:email:auth:" + emailAuthRequest.email()));
    }

    @Test
    @DisplayName("[실패] 회원가입 이메일 인증 코드 요청 시 부산대 이메일이 아니면 요청 불가하다.")
    void 회원가입_이메일_인증_코드_요청_시_부산대_이메일이_아니면_요청_불가하다() {
        final EmailAuthRequest notPusanEmailRequest = new EmailAuthRequest("qwer123@gmail.com");

        assertThatThrownBy(() -> {
            memberCommandService.signUpEmailAuth(notPusanEmailRequest);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_PUSAN_UNIVERSITY_EMAIL.errorMessage());
    }

    @Test
    @DisplayName("[실패] 회원가입 이메일 인증 코드 발송을 5회 이상 요청하면 24시간 동안 인증 불가하다.")
    void 회원가입_이메일_인증_코드_발송을_5회_이상_요청하면_24시간_동안_인증_불가하다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        memberCommandService.signUpEmailAuth(emailAuthRequest);

        assertThatThrownBy(() -> {
            memberCommandService.signUpEmailAuth(emailAuthRequest);
        }).isInstanceOf(MemberException.class).hasMessage(EMAIL_AUTH_LIMIT_EXCEEDED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 회원가입과 비밀번호 변경 이메일 인증 코드 발송 제한 count는 합계 계산된다.")
    void 회원가입과_비밀번호_변경_이메일_인증_코드_발송_제한_count는_합계_계산된다() {
        final String newMemberEmail = "example100@pusan.ac.kr";
        authRedisUtil.delete("email:auth:count:" + newMemberEmail);

        memberCommandService.signUpEmailAuth(new EmailAuthRequest(newMemberEmail));
        memberCommandService.signUpEmailAuth(new EmailAuthRequest(newMemberEmail));
        memberCommandService.signUpEmailAuth(new EmailAuthRequest(newMemberEmail));

        memberRepository.save(createMemberWithUniqueNum(100));

        memberCommandService.signInEmailAuth(new EmailAuthRequest(newMemberEmail));
        memberCommandService.signInEmailAuth(new EmailAuthRequest(newMemberEmail));

        assertThatThrownBy(() -> {
            memberCommandService.signInEmailAuth(new EmailAuthRequest(newMemberEmail));
        }).isInstanceOf(MemberException.class).hasMessage(EMAIL_AUTH_LIMIT_EXCEEDED.errorMessage());
    }

    @Test
    @DisplayName("[성공] 회원가입 이메일 인증이 완료되면 인증 코드는 삭제된다.")
    void 회원가입_이메일_인증이_완료되면_인증_코드는_삭제된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        assertThat(authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email())).isNull();
    }

    @Test
    @DisplayName("[성공] 회원가입 이메일 인증이 완료되면 인증 완료 코드가 발급된다")
    void 회원가입_이메일_인증이_완료되면_인증_완료_코드가_발급된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        assertTrue(authRedisUtil.exists("signup:email:verified:" + emailAuthRequest.email()));
    }

    @Test
    @DisplayName("[실패] 인증 코드가 일치하지 않으면 인증 불가하다.")
    void 인증_코드가_일치하지_않으면_인증_불가하다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest misMatchCodeRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                "misMatchCode");

        assertThatThrownBy(() -> {
            memberCommandService.confirmSignUpEmailAuth(misMatchCodeRequest);
        }).isInstanceOf(MemberException.class).hasMessage(CANNOT_MATCH_EMAIL_AUTH_CODE.errorMessage());
    }

    @Test
    @DisplayName("[성공] 인증 코드 TTL은 5분이다.")
    @Disabled // 테스트에 따라 4와 5가 랜덤. 필요 시 Disable 해제하고 테스트
    void 인증_코드_TTL은_5분이다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);

        // 인증 시간은 테스트 시작부터 줄어들기 때문에(내림 처리됨) 4분으로 설정 (실제는 5분)
        assertThat(authRedisUtil.ttl("signup:email:auth:" + emailAuthRequest.email(), TimeUnit.MINUTES)).isEqualTo(4);
    }

    @Test
    @DisplayName("[성공] 인증 완료 코드 TTL은 10분이다.")
    @Disabled // 테스트에 따라 9와 10이 랜덤. 필요 시 Disable 해제하고 테스트
    void 인증_완료_코드_TTL은_10분이다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        // 인증 시간은 테스트 시작부터 줄어들기 때문에(내림 처리됨) 9분으로 설정 (실제는 10분)
        assertThat(authRedisUtil.ttl("signup:email:verified:" + emailAuthRequest.email(), TimeUnit.MINUTES)).isEqualTo(9);
    }

    @Test
    @DisplayName("[성공] 인증 완료 코드가 있다면 회원가입은 정상적으로 이뤄진다.")
    void 인증_완료_코드가_있다면_회원가입은_정상적으로_이뤄진다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);
        final SignUpRequest request = new SignUpRequest("이름", "202512345", "qwer1234@pusan.ac.kr", "qwer123!");

        memberCommandService.signUp(request);

        final Member member = memberRepository.findByStudentId("202512345").orElseThrow();
        assertThat(member.getName()).isEqualTo("이름");
    }

    @Test
    @DisplayName("[실패] 인증 완료 코드가 없다면 회원가입은 불가하다.")
    void 인증_완료_코드가_없다면_회원가입은_불가하다() {
        final SignUpRequest notExistAuthCodeRequest = new SignUpRequest("이름", "202512345", "qwer1234@pusan.ac.kr",
                "qwer123!");

        assertThatThrownBy(() -> {
            memberCommandService.signUp(notExistAuthCodeRequest);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_VERIFIED_EMAIL_AUTH.errorMessage());
    }

    @Test
    @DisplayName("[성공] 관리자가 권한을 등록한 회원은 가입 시 이메일과 비밀번호가 업데이트 된다. ")
    void 관리자가_권한을_등록한_회원은_가입_시_이메일과_비밀번호가_업데이트_된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);
        final SignUpRequest teamLeaderRequest = new SignUpRequest(teamLeader.getName(), teamLeader.getStudentId(),
                "qwer1234@pusan.ac.kr",
                "changePassword");

        memberCommandService.signUp(teamLeaderRequest);

        final Member member = memberRepository.findByStudentId(teamLeader.getStudentId()).orElseThrow();
        assertThat(memberRepository.count()).isEqualTo(1);
        assertThat(passwordEncoder.matches(teamLeaderRequest.password(), member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("[성공] 회원가입이 완료되면 인증 완료 코드는 삭제된다.")
    void 회원가입이_완료되면_인증_완료_코드는_삭제된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                authRedisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);
        final SignUpRequest teamLeaderRequest = new SignUpRequest(teamLeader.getName(), teamLeader.getStudentId(),
                "qwer1234@pusan.ac.kr", "changePassword");

        memberCommandService.signUp(teamLeaderRequest);

        assertThat(authRedisUtil.get("signup:email:verified:" + emailAuthRequest.email())).isNull();
    }

    @Test
    @DisplayName("[성공] 가입된 회원은 로그인 할 수 있다.")
    void 가입된_회원은_로그인_할_수_있다() {
        final SignInRequest request = new SignInRequest(teamLeader.getEmail(), "123456789");

        final SignInResponse response = memberCommandService.signIn(request);

        assertThat(response.memberId()).isEqualTo(teamLeader.getId());
        assertThat(response.token()).isNotEmpty();
    }

    @Test
    @DisplayName("[실패] 일반 회원이 소셜 로그인 시도 시 예외가 발생한다.")
    void 일반_회원이_소셜_로그인_시도_시_예외가_발생한다() {
        GoogleOAuth2UserService target = AopTestUtils.getTargetObject(googleOAuth2UserService);

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(target, "validateSocialMember", teamLeader, "some-social-id")
        ).isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> {
                    OAuth2AuthenticationException ex = (OAuth2AuthenticationException) e;
                    assertThat(ex.getError().getErrorCode()).isEqualTo(GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN.name());
                });
    }

    @Test
    @DisplayName("[성공] 신규 소셜 회원은 자동 가입된다.")
    void 신규_소셜_회원은_자동_가입된다() {
        ReflectionTestUtils.invokeMethod(googleOAuth2UserService, "registerNewSocialMember", "김태윤", "pykido@gmail.com", "google-sub-999");

        final Member savedMember = memberRepository.findByEmail("pykido@gmail.com").orElseThrow();
        assertThat(savedMember.isSocialMember()).isTrue();
        assertThat(savedMember.getName()).isEqualTo("김태윤");
    }

    @Test
    @DisplayName("[성공] 학번 수정이 정상적으로 이루어진다.")
    void 학번_수정이_정상적으로_이루어진다() {
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("cscs@pusan.ac.kr", "google-123456789"));
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");
        assertThat(socialMember.getStudentId()).isNull();

        memberCommandService.updateStudentId(socialMember.getId(), request);

        final Member updated = memberRepository.findById(socialMember.getId()).orElseThrow();
        assertThat(updated.getStudentId()).isEqualTo("202512345");
    }

    @Test
    @DisplayName("[실패] 소셜 회원이 아니면 학번 수정이 불가하다.")
    void 소셜_회원이_아니면_학번_수정이_불가하다() {
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");

        assertThatThrownBy(() ->
                memberCommandService.updateStudentId(teamLeader.getId(), request)
        ).isInstanceOf(MemberException.class)
                .hasMessage(CANNOT_UPDATE_STUDENT_ID.errorMessage());
    }

    @Test
    @DisplayName("[실패] 부산대 메일이 아닌 소셜 회원은 학번 수정이 불가하다.")
    void 부산대_메일이_아닌_소셜_회원은_학번_수정이_불가하다() {
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("cscs@gmail.com", "google-999"));
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");

        assertThatThrownBy(() ->
                memberCommandService.updateStudentId(socialMember.getId(), request)
        ).isInstanceOf(MemberException.class)
                .hasMessage(CANNOT_UPDATE_STUDENT_ID.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 학번이 있는 소셜 회원은 학번 수정이 불가하다.")
    void 이미_학번이_있는_소셜_회원은_학번_수정이_불가하다() {
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("already@pusan.ac.kr", "google-999"));
        socialMember.updateStudentId("202011111");
        memberRepository.save(socialMember);
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");

        assertThatThrownBy(() ->
                memberCommandService.updateStudentId(socialMember.getId(), request)
        ).isInstanceOf(MemberException.class)
                .hasMessage(CANNOT_UPDATE_STUDENT_ID.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복된 학번으로는 수정이 불가하다.")
    void 중복된_학번으로는_수정이_불가하다() {
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("cscs@pusan.ac.kr", "google-123456789"));
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest(teamLeader.getStudentId());

        assertThatThrownBy(() ->
                memberCommandService.updateStudentId(socialMember.getId(), request)
        ).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("[성공] 기존 프로필 이미지가 없어도 새 이미지 저장이 호출된다.")
    void 기존_프로필_이미지가_없어도_새_이미지_저장이_호출된다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "profile.jpg", "image/jpeg", "content".getBytes());

        // when
        memberCommandService.modifyProfileImage(teamLeader, image);

        // then
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(teamLeader.getId()), eq(MEMBER), eq(PROFILE));
    }

    @Test
    @DisplayName("[성공] 기존 프로필 이미지가 있으면 새 이미지 저장 후 기존 이미지 삭제가 호출된다.")
    void 기존_프로필_이미지가_있으면_새_이미지_저장_후_기존_이미지_삭제가_호출된다() {
        // given
        final File savedFile = fileRepository.save(FileFixture.createMemberProfileFile(teamLeader.getId()));
        final MockMultipartFile image = new MockMultipartFile("image", "new_profile.jpg", "image/jpeg", "content".getBytes());

        // when
        memberCommandService.modifyProfileImage(teamLeader, image);

        // then
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(teamLeader.getId()), eq(MEMBER), eq(PROFILE));
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
    }

    @Test
    @DisplayName("[성공] 기존 프로필 이미지가 없으면 이미지 변경 시 삭제가 호출되지 않는다.")
    void 기존_프로필_이미지가_없으면_이미지_변경_시_삭제가_호출되지_않는다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "profile.jpg", "image/jpeg", "content".getBytes());

        // when
        memberCommandService.modifyProfileImage(teamLeader, image);

        // then
        verify(fileStorageUtil, never()).deleteFile(any());
    }

    @Test
    @DisplayName("[성공] 프로필 이미지가 있으면 삭제가 호출된다.")
    void 프로필_이미지가_있으면_삭제가_호출된다() {
        // given
        final File savedFile = fileRepository.save(FileFixture.createMemberProfileFile(teamLeader.getId()));

        // when
        memberCommandService.deleteProfileImage(teamLeader);

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
    }

    @Test
    @DisplayName("[성공] 프로필 이미지가 없으면 삭제가 호출되지 않는다.")
    void 프로필_이미지가_없으면_삭제가_호출되지_않는다() {
        // when
        memberCommandService.deleteProfileImage(teamLeader);

        // then
        verify(fileStorageUtil, never()).deleteFile(any());
    }

    @Test
    @DisplayName("[성공] 일반 회원 탈퇴 시 DB에서 조회되지 않는다.")
    void 일반_회원_탈퇴_시_DB에서_조회되지_않는다() {
        // when
        memberCommandService.withdraw(teamLeader);

        // then
        assertThat(memberRepository.findById(teamLeader.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] 소셜 회원 탈퇴 시 DB에서 조회되지 않는다.")
    void 소셜_회원_탈퇴_시_DB에서_조회되지_않는다() {
        // given
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("social@pusan.ac.kr", "google-abc123"));
        when(googleTokenManager.get(socialMember.getId())).thenReturn(java.util.Optional.empty());

        // when
        memberCommandService.withdraw(socialMember);

        // then
        assertThat(memberRepository.findById(socialMember.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] 일반 회원 탈퇴 시 Google 토큰 해제를 시도하지 않는다.")
    void 일반_회원_탈퇴_시_Google_토큰_해제를_시도하지_않는다() {
        // when
        memberCommandService.withdraw(teamLeader);

        // then
        verify(googleTokenManager, never()).get(any());
    }

    @Test
    @DisplayName("[성공] 소셜 회원 탈퇴 시 Google 토큰 해제를 시도한다.")
    void 소셜_회원_탈퇴_시_Google_토큰_해제를_시도한다() {
        // given
        final Member socialMember = memberRepository.save(MemberFixture.createSocialMember("social2@pusan.ac.kr", "google-def456"));
        when(googleTokenManager.get(socialMember.getId())).thenReturn(java.util.Optional.empty());

        // when
        memberCommandService.withdraw(socialMember);

        // then
        verify(googleTokenManager, times(1)).get(socialMember.getId());
    }

    @Test
    @DisplayName("[성공] 관리자 강제 탈퇴 시 해당 회원은 DB에서 조회되지 않는다.")
    void 관리자_강제_탈퇴_시_해당_회원은_DB에서_조회되지_않는다() {
        // when
        memberCommandService.withdrawByAdmin(teamLeader.getId());

        // then
        assertThat(memberRepository.findById(teamLeader.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 회원을 강제 탈퇴하면 예외가 발생한다.")
    void 존재하지_않는_회원을_강제_탈퇴하면_예외가_발생한다() {
        // given
        final Long nonExistentId = 999999L;

        // when & then
        assertThatThrownBy(() -> memberCommandService.withdrawByAdmin(nonExistentId))
                .isInstanceOf(MemberException.class)
                .hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] GitHub 링크가 정상적으로 수정된다.")
    void GitHub_링크가_정상적으로_수정된다() {
        final GithubUrlUpdateRequest request = new GithubUrlUpdateRequest("https://github.com/hongjiyeon");

        memberCommandService.updateGithubUrl(teamLeader.getId(), request);

        final Member updatedMember = memberRepository.findById(teamLeader.getId()).orElseThrow();
        assertThat(updatedMember.getGithubUrl()).isEqualTo("https://github.com/hongjiyeon");
    }

    @Test
    @DisplayName("[성공] GitHub 링크를 null로 수정할 수 있다.")
    void GitHub_링크를_null로_수정할_수_있다() {
        memberCommandService.updateGithubUrl(teamLeader.getId(), new GithubUrlUpdateRequest("https://github.com/test"));
        memberCommandService.updateGithubUrl(teamLeader.getId(), new GithubUrlUpdateRequest(null));

        final Member updatedMember = memberRepository.findById(teamLeader.getId()).orElseThrow();
        assertThat(updatedMember.getGithubUrl()).isNull();
    }

    @Test
    @DisplayName("[성공] 프로필 공개 여부가 정상적으로 변경된다.")
    void 프로필_공개_여부가_정상적으로_변경된다() {
        final ProfileVisibilityUpdateRequest request = new ProfileVisibilityUpdateRequest(false);

        memberCommandService.updateProfileVisibility(teamLeader.getId(), request);

        final Member updatedMember = memberRepository.findById(teamLeader.getId()).orElseThrow();
        assertThat(updatedMember.getIsProfilePublic()).isFalse();
    }

    @Test
    @DisplayName("[성공] 프로필 비공개에서 공개로 변경할 수 있다.")
    void 프로필_비공개에서_공개로_변경할_수_있다() {
        memberCommandService.updateProfileVisibility(teamLeader.getId(), new ProfileVisibilityUpdateRequest(false));
        memberCommandService.updateProfileVisibility(teamLeader.getId(), new ProfileVisibilityUpdateRequest(true));

        final Member updatedMember = memberRepository.findById(teamLeader.getId()).orElseThrow();
        assertThat(updatedMember.getIsProfilePublic()).isTrue();
    }
}
