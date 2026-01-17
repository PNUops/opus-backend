package com.opus.opus.modules.member.application;

import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.FAILED_TO_GET_SOCIAL_USER_INFO;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.OAUTH_AUTHORIZATION_FAILED;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.SOCIAL_LOGIN_FAILED_AUTH_CODE;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.SOCIAL_LOGIN_SERVER_ERROR;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.USER_DENIED_AUTHORIZATION;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_CHANGE_SAME_PASSWORD;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_PASSWORD;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.global.util.AuthRedisUtil;
import com.opus.opus.global.util.MailUtil;
import com.opus.opus.global.util.oauth.component.GoogleOauth;
import com.opus.opus.global.util.oauth.dto.GoogleUser;
import com.opus.opus.global.util.oauth.exception.OAuthException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.PasswordUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberRepository memberRepository;

    private final MemberConvenience memberConvenience;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MailUtil mailUtil;
    private final AuthRedisUtil authRedisUtil;
    private final GoogleOauth googleOauth;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int AUTH_CODE_LENGTH = 10;
    private static final char[] AUTH_CODE_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final String PASSWORD_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    private static final long SIGNUP_AUTH_CODE_TTL = 5L;
    private static final long SIGNUP_VERIFIED_TTL = 10L;
    private static final String SIGNUP_EMAIL_AUTH_KEY_PREFIX = "signup:email:auth:";
    private static final String SIGNUP_EMAIL_VERIFIED_KEY_PREFIX = "signup:email:verified:";

    private static final long SIGNIN_AUTH_CODE_TTL = 5L;
    private static final long SIGNIN_VERIFIED_TTL = 10L;
    private static final String SIGNIN_EMAIL_AUTH_KEY_PREFIX = "signin:email:auth:";
    private static final String SIGNIN_EMAIL_VERIFIED_KEY_PREFIX = "signin:email:verified:";

    private static final String VERIFIED_VALUE = "true";

    public void signUp(final SignUpRequest request) {
        final String email = request.email();
        verifyVerifiedKey(signUpVerifiedKey(email));

        final String encodingPassword = passwordEncoder.encode(request.password());
        memberConvenience.checkIsDuplicateEmail(email);

        memberRepository.findByStudentIdAndName(request.studentId(), request.name())
                .ifPresentOrElse(
                        member -> member.updateTeamLeaderInfo(email, encodingPassword),
                        () -> registerNewMember(request.name(), request.studentId(), email, encodingPassword)
                );

        authRedisUtil.delete(signUpVerifiedKey(email));
    }

    public void signUpEmailAuth(final EmailAuthRequest request) {
        final String email = request.email();
        memberConvenience.validatePusanDomain(email);

        final String code = generateRandomAuthCode();

        authRedisUtil.set(signUpAuthKey(email), code, SIGNUP_AUTH_CODE_TTL, TimeUnit.MINUTES);
        sendAuthCodeMail(email, code);
    }

    public void confirmSignUpEmailAuth(final EmailAuthConfirmRequest request) {
        final String email = request.email();
        validateSignUpAuthCode(email, request.authCode());

        authRedisUtil.delete(signUpAuthKey(email));
        authRedisUtil.set(signUpVerifiedKey(email), VERIFIED_VALUE, SIGNUP_VERIFIED_TTL, TimeUnit.MINUTES);
    }

    public SignInResponse signIn(final SignInRequest request) {
        final Member member = memberConvenience.getValidateExistMemberByEmail(request.email());
        checkCorrectPassword(member.getPassword(), request.password());

        final List<String> roles = member.getRoles().stream()
                .map(MemberRoleType::toString)
                .toList();

        final String token = jwtProvider.createToken(String.valueOf(member.getId()), roles, member.getName());
        return SignInResponse.from(member, token);
    }

    public void signInEmailAuth(final EmailAuthRequest request) {
        final String email = request.email();
        memberConvenience.validateExistMemberByEmail(email);

        final String code = generateRandomAuthCode();

        authRedisUtil.set(signInAuthKey(email), code, SIGNIN_AUTH_CODE_TTL, TimeUnit.MINUTES);
        sendAuthCodeMail(email, code);
    }

    public void confirmSignInEmailAuth(final EmailAuthConfirmRequest request) {
        final String email = request.email();
        memberConvenience.validateExistMemberByEmail(email);

        validateSignInAuthCode(email, request.authCode());

        authRedisUtil.delete(signInAuthKey(email));
        authRedisUtil.set(signInVerifiedKey(email), VERIFIED_VALUE, SIGNIN_VERIFIED_TTL, TimeUnit.MINUTES);
    }

    public void updatePassword(final PasswordUpdateRequest request) {
        final String email = request.email();
        final Member member = memberConvenience.getValidateExistMemberByEmail(email);

        verifyVerifiedKey(signInVerifiedKey(email));

        checkEqualPassword(request.newPassword(), member);
        member.updatePassword(passwordEncoder.encode(request.newPassword()));

        authRedisUtil.delete(signInVerifiedKey(email));
    }

    public String getGoogleOAuthRedirectURL() {
        try {
            return googleOauth.getOauthRedirectURL();
        } catch (Exception e) {
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }
    }

    public SignInResponse getGoogleOAuthCallback(final String code, final String state, final String error) {
        try {
            validateGoogleOAuthRequest(code, state, error);

            final GoogleUser googleUser = googleOauth.getUserInfoByCode(code, GoogleUser.class);

            return memberRepository.findByEmail(googleUser.email())
                    .map(this::processExistingMemberLogin) // 기존 회원 로그인 처리
                    .orElseGet(() -> processNewMemberSignUp(googleUser)); // 새로운 회원 가입 처리

        } catch (OAuthException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new OAuthException(FAILED_TO_GET_SOCIAL_USER_INFO);
        } catch (Exception e) {
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }
    }

    private SignInResponse processExistingMemberLogin(final Member member) {
        final List<String> roles = member.getRoles().stream()
                .map(MemberRoleType::toString)
                .toList();
        final String token = jwtProvider.createToken(String.valueOf(member.getId()), roles, member.getName());

        return SignInResponse.from(member, token);
    }

    private SignInResponse processNewMemberSignUp(final GoogleUser googleUser) {
        try {
            final Member newMember = getRegisterNewMember(googleUser.name(), googleUser.email());
            return processExistingMemberLogin(newMember);

        } catch (Exception e) {
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }
    }

    private Member getRegisterNewMember(final String name, final String email) {
        final String randomPassword = generateRandomPassword();
        final String uniqueStudentId = "fake_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        return memberRepository.save(Member.builder()
                .name(name)
                .studentId(uniqueStudentId)
                .email(email)
                .password(randomPassword)
                .roles(Set.of(ROLE_회원))
                .build());
    }

    private String generateRandomPassword() {
        final int passwordLength = 32;

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            password.append(PASSWORD_POOL.charAt(SECURE_RANDOM.nextInt(PASSWORD_POOL.length())));
        }

        return passwordEncoder.encode(password.toString());
    }

    private void verifyVerifiedKey(final String verifiedKey) {
        if (authRedisUtil.get(verifiedKey) == null) {
            throw new MemberException(NOT_VERIFIED_EMAIL_AUTH);
        }
    }

    private void registerNewMember(final String name, final String studentId, final String email,
                                   final String password) {
        memberConvenience.checkIsDuplicateStudentId(studentId);

        memberRepository.save(Member.builder()
                .name(name)
                .studentId(studentId)
                .email(email)
                .password(password)
                .roles(Set.of(ROLE_회원))
                .build());
    }

    private static String generateRandomAuthCode() {
        final char[] buf = new char[AUTH_CODE_LENGTH];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = AUTH_CODE_POOL[SECURE_RANDOM.nextInt(AUTH_CODE_POOL.length)];
        }
        return new String(buf);
    }

    private void sendAuthCodeMail(final String email, final String authCode) {
        final List<String> userList = new ArrayList<>(List.of(email));
        final String subject = "SW프로젝트관리시스템 인증코드 발송 메일입니다.";
        final String text = "인증코드는 " + authCode + " 입니다.";
        mailUtil.sendMail(userList, subject, text);
    }

    private void validateAuthCode(final String authKey, final String inputCode) {
        Optional.ofNullable(authRedisUtil.get(authKey))
                .map(code -> {
                    if (!code.equals(inputCode)) {
                        throw new MemberException(CANNOT_MATCH_EMAIL_AUTH_CODE);
                    }
                    return code;
                })
                .orElseThrow(() -> new MemberException(CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE));
    }

    private void validateSignUpAuthCode(final String email, final String inputCode) {
        validateAuthCode(signUpAuthKey(email), inputCode);
    }

    private void validateSignInAuthCode(final String email, final String inputCode) {
        validateAuthCode(signInAuthKey(email), inputCode);
    }

    private void checkCorrectPassword(final String savePassword, final String inputPassword) {
        if (!passwordEncoder.matches(inputPassword, savePassword)) {
            throw new MemberException(CANNOT_MATCH_PASSWORD);
        }
    }

    private void checkEqualPassword(final String newPassword, final Member member) {
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new MemberException(CANNOT_CHANGE_SAME_PASSWORD);
        }
    }

    private static String signUpAuthKey(final String email) {
        return SIGNUP_EMAIL_AUTH_KEY_PREFIX + email;
    }

    private static String signUpVerifiedKey(final String email) {
        return SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + email;
    }

    private static String signInAuthKey(final String email) {
        return SIGNIN_EMAIL_AUTH_KEY_PREFIX + email;
    }

    private static String signInVerifiedKey(final String email) {
        return SIGNIN_EMAIL_VERIFIED_KEY_PREFIX + email;
    }

    private void validateGoogleOAuthRequest(final String code, final String state, final String error) {
        validateState(state);

        if (error != null) {
            throw new OAuthException(USER_DENIED_AUTHORIZATION);
        }

        if (code == null || code.isBlank()) {
            throw new OAuthException(SOCIAL_LOGIN_FAILED_AUTH_CODE);
        }
    }

    private void validateState(final String state) {
        if (state == null || state.isBlank()) {
            throw new OAuthException(OAUTH_AUTHORIZATION_FAILED);
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }

        HttpServletRequest request = attributes.getRequest();
        String sessionId = request.getSession().getId();
        String stateKey = "oauth:state:" + sessionId + ":" + state;
        String storedState = authRedisUtil.get(stateKey);

        if (storedState == null) {
            throw new OAuthException(OAUTH_AUTHORIZATION_FAILED);
        }

        authRedisUtil.delete(stateKey);
    }
}
