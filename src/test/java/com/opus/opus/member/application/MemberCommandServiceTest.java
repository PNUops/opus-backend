package com.opus.opus.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MemberCommandServiceTest extends IntegrationTest {

    @Autowired
    private MemberCommandService memberCommandService;

    @Autowired
    private MemberRepository memberRepository;

    private Member teamLeader;
    private EmailAuthRequest emailAuthRequest;

    @BeforeEach
    void setUp() {
        teamLeader = memberRepository.save(MemberFixture.createMember());
        emailAuthRequest = new EmailAuthRequest("qwer1234@pusan.ac.kr");
    }

    @Test
    @DisplayName("[성공] 회원가입 시 이메일 인증 코드가 정상 발급된다.")
    void 회원가입_시_이메일_인증_코드가_정상_발급된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);

        assertTrue(redisUtil.exists("signup:email:auth:" + emailAuthRequest.email()));
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
    @DisplayName("[성공] 회원가입 이메일 인증이 완료되면 인증 코드는 삭제된다.")
    void 회원가입_이메일_인증이_완료되면_인증_코드는_삭제된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        assertThat(redisUtil.get("signup:email:auth:" + emailAuthRequest.email())).isNull();
    }

    @Test
    @DisplayName("[성공] 회원가입 이메일 인증이 완료되면 인증 완료 코드가 발급된다")
    void 회원가입_이메일_인증이_완료되면_인증_완료_코드가_발급된다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        assertTrue(redisUtil.exists("signup:email:verified:" + emailAuthRequest.email()));
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
    void 인증_코드_TTL은_5분이다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);

        // 인증 시간은 테스트 시작부터 줄어들기 때문에(내림 처리됨) 4분으로 설정 (실제는 5분)
        assertThat(redisUtil.ttl("signup:email:auth:" + emailAuthRequest.email(), TimeUnit.MINUTES)).isEqualTo(4);
    }

    @Test
    @DisplayName("[성공] 인증 완료 코드 TTL은 10분이다.")
    void 인증_완료_코드_TTL은_10분이다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));

        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);

        // 인증 시간은 테스트 시작부터 줄어들기 때문에(내림 처리됨) 9분으로 설정 (실제는 10분)
        assertThat(redisUtil.ttl("signup:email:verified:" + emailAuthRequest.email(), TimeUnit.MINUTES)).isEqualTo(9);
    }

    @Test
    @DisplayName("[성공] 인증 완료 코드가 있다면 회원가입은 정상적으로 이뤄진다.")
    void 인증_완료_코드가_있다면_회원가입은_정상적으로_이뤄진다() {
        memberCommandService.signUpEmailAuth(emailAuthRequest);
        final EmailAuthConfirmRequest emailAuthConfirmRequest = new EmailAuthConfirmRequest(emailAuthRequest.email(),
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
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
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
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
                redisUtil.get("signup:email:auth:" + emailAuthRequest.email()));
        memberCommandService.confirmSignUpEmailAuth(emailAuthConfirmRequest);
        final SignUpRequest teamLeaderRequest = new SignUpRequest(teamLeader.getName(), teamLeader.getStudentId(),
                "qwer1234@pusan.ac.kr", "changePassword");

        memberCommandService.signUp(teamLeaderRequest);

        assertThat(redisUtil.get("signup:email:verified:" + emailAuthRequest.email())).isNull();
    }

    @Test
    @DisplayName("[성공] 가입된 회원은 로그인 할 수 있다.")
    void 가입된_회원은_로그인_할_수_있다() {
        final SignInRequest request = new SignInRequest(teamLeader.getEmail(), "123456789");

        final SignInResponse response = memberCommandService.signIn(request);

        assertThat(response.memberId()).isEqualTo(teamLeader.getId());
        assertThat(response.token()).isNotEmpty();
    }
}
