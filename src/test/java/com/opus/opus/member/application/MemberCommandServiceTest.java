package com.opus.opus.member.application;

import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.OAUTH_AUTHORIZATION_FAILED;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.SOCIAL_LOGIN_FAILED_AUTH_CODE;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.USER_DENIED_AUTHORIZATION;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_UPDATE_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opus.opus.global.util.oauth.dto.GoogleUser;
import com.opus.opus.global.util.oauth.dto.OAuthResult;
import com.opus.opus.global.util.oauth.exception.OAuthException;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.request.StudentIdUpdateRequest;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class MemberCommandServiceTest extends IntegrationTest {

    @Autowired
    private MemberCommandService memberCommandService;

    @Autowired
    private MemberRepository memberRepository;

    private Member teamLeader;
    private EmailAuthRequest emailAuthRequest;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        teamLeader = memberRepository.save(MemberFixture.createMember());
        emailAuthRequest = new EmailAuthRequest("qwer1234@pusan.ac.kr");

        when(googleOauth.createOAuthStateKey(anyString(), anyString())).thenCallRealMethod(); // state key값이 null이 되는 문제 방지하기 위해 실제 메서드 호출
    }

    private void setUpMockRequest() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setSession(new MockHttpSession());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    private String getSessionId() {
        return mockRequest.getSession().getId();
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
    @DisplayName("[실패] state가 null이면 인증 실패한다")
    void state가_null이면_인증_실패한다() {
        final String code = "code";
        final String state = null;

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback(code, state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(OAUTH_AUTHORIZATION_FAILED.errorMessage());
    }

    @Test
    @DisplayName("[실패] state가 빈 문자열이면 인증 실패한다")
    void state가_빈_문자열이면_인증_실패한다() {
        final String code = "code";
        final String state = "";

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback(code, state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(OAUTH_AUTHORIZATION_FAILED.errorMessage());
    }

    @Test
    @DisplayName("[실패] Redis에 저장되지 않은 state면 인증 실패한다")
    void Redis에_저장되지_않은_state면_인증_실패한다() {
        final String code = "code";
        final String state = "invalidState";

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback(code, state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(OAUTH_AUTHORIZATION_FAILED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 세션의 state로는 인증 실패한다 (CSRF 방어)")
    void 다른_세션의_state로는_인증_실패한다() {
        setUpMockRequest();
        final String attackerSessionId = "attacker-session-id";
        final String state = "state";
        final String attackerStateKey = "oauth:state:" + attackerSessionId + ":" + state;
        authRedisUtil.set(attackerStateKey, "valid", 5L, TimeUnit.MINUTES);

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback("code", state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(OAUTH_AUTHORIZATION_FAILED.errorMessage());
    }

    @Test
    @DisplayName("[실패] error 파라미터가 있으면 사용자 권한 거부로 처리된다")
    void error_파라미터가_있으면_사용자_권한_거부로_처리된다() {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback("code", state, "access_denied");
        }).isInstanceOf(OAuthException.class)
                .hasMessage(USER_DENIED_AUTHORIZATION.errorMessage());
    }

    @Test
    @DisplayName("[실패] code가 null이면 인증 실패한다")
    void code가_null이면_인증_실패한다() {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback(null, state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(SOCIAL_LOGIN_FAILED_AUTH_CODE.errorMessage());
    }

    @Test
    @DisplayName("[실패] code가 빈 문자열이면 인증 실패한다")
    void code가_빈_문자열이면_인증_실패한다() {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);

        assertThatThrownBy(() -> {
            memberCommandService.getGoogleOAuthCallback("", state, null);
        }).isInstanceOf(OAuthException.class)
                .hasMessage(SOCIAL_LOGIN_FAILED_AUTH_CODE.errorMessage());
    }

    @Test
    @DisplayName("[성공] 기존 회원은 OAuth 로그인 처리된다")
    void 기존_회원은_OAuth_로그인_처리된다() throws Exception {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);
        final GoogleUser mockGoogleUser = new GoogleUser(teamLeader.getEmail(), teamLeader.getName());
        final OAuthResult<GoogleUser> mockResult = new OAuthResult<>(mockGoogleUser, "accessToken", "refreshToken");
        when(googleOauth.getUserInfoByCode(anyString(), eq(GoogleUser.class)))
                .thenReturn(mockResult);

        SignInResponse response = memberCommandService.getGoogleOAuthCallback("code", state, null);

        assertThat(response.memberId()).isEqualTo(teamLeader.getId());
        assertThat(response.token()).isNotEmpty();
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 신규 회원은 자동 가입 후 OAuth 로그인 처리된다")
    void 신규_회원은_자동_가입_후_OAuth_로그인_처리된다() throws Exception {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);
        final GoogleUser mockGoogleUser = new GoogleUser("kty@gmail.com", "김태윤");
        final OAuthResult<GoogleUser> mockResult = new OAuthResult<>(mockGoogleUser, "accessToken", "refreshToken");
        when(googleOauth.getUserInfoByCode(anyString(), eq(GoogleUser.class)))
                .thenReturn(mockResult);

        SignInResponse response = memberCommandService.getGoogleOAuthCallback("code", state, null);

        assertThat(response.name()).isEqualTo("김태윤");
        assertThat(response.token()).isNotEmpty();
        assertThat(memberRepository.count()).isEqualTo(2);
        Member newMember = memberRepository.findByEmail("kty@gmail.com").orElseThrow();
        assertThat(newMember.getStudentId()).startsWith("fake_");
    }

    @Test
    @DisplayName("[성공] 검증 성공 시 state가 Redis에서 삭제된다")
    void 검증_성공_시_state가_Redis에서_삭제된다() throws Exception {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);
        final GoogleUser mockGoogleUser = new GoogleUser(teamLeader.getEmail(), teamLeader.getName());
        final OAuthResult<GoogleUser> mockResult = new OAuthResult<>(mockGoogleUser, "accessToken", "refreshToken");
        when(googleOauth.getUserInfoByCode(anyString(), eq(GoogleUser.class)))
                .thenReturn(mockResult);

        memberCommandService.getGoogleOAuthCallback("code", state, null);

        assertThat(authRedisUtil.exists(stateKey)).isFalse();
    }

    @Test
    @DisplayName("[성공] OAuth 로그인 시 토큰이 Redis에 저장된다")
    void OAuth_로그인_시_토큰이_Redis에_저장된다() throws Exception {
        setUpMockRequest();
        final String state = "state";
        final String stateKey = "oauth:state:" + getSessionId() + ":" + state;
        authRedisUtil.set(stateKey, "valid", 5L, TimeUnit.MINUTES);
        final GoogleUser mockGoogleUser = new GoogleUser(teamLeader.getEmail(), teamLeader.getName());
        final OAuthResult<GoogleUser> mockResult = new OAuthResult<>(mockGoogleUser, "testAccessToken", "testRefreshToken");
        when(googleOauth.getUserInfoByCode(anyString(), eq(GoogleUser.class)))
                .thenReturn(mockResult);

        memberCommandService.getGoogleOAuthCallback("code", state, null);

        final String tokenKey = "oauth:google:token:" + teamLeader.getId();
        assertThat(authRedisUtil.exists(tokenKey)).isTrue();
        assertThat(authRedisUtil.get(tokenKey)).isEqualTo("testAccessToken:testRefreshToken");
    }

    @Test
    @DisplayName("[성공] 구글 연동 해제 시 Redis 토큰이 삭제된다")
    void 구글_연동_해제_시_Redis_토큰이_삭제된다() {
        final String tokenKey = "oauth:google:token:" + teamLeader.getId();
        authRedisUtil.set(tokenKey, "accessToken:refreshToken", 3L, TimeUnit.HOURS);

        memberCommandService.unlinkGoogleAccount(teamLeader.getId());

        assertThat(authRedisUtil.exists(tokenKey)).isFalse();
    }

    @Test
    @DisplayName("[성공] 구글 연동 해제 시 revokeToken이 호출된다")
    void 구글_연동_해제_시_revokeToken이_호출된다() {
        final String tokenKey = "oauth:google:token:" + teamLeader.getId();
        authRedisUtil.set(tokenKey, "testAccessToken:testRefreshToken", 3L, TimeUnit.HOURS);

        memberCommandService.unlinkGoogleAccount(teamLeader.getId());

        verify(googleOauth).revokeToken("testAccessToken");
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
}
