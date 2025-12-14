package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_PASSWORD;
import static com.opus.opus.modules.member.exception.MemberExceptionType.EMAIL_AUTH_CODE_EXPIRED;
import static com.opus.opus.modules.member.exception.MemberExceptionType.EMAIL_AUTH_CODE_MISMATCH;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.global.util.MailUtil;
import com.opus.opus.global.util.RedisUtil;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberRepository memberRepository;

    private final MemberConvenience memberConvenience;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MailUtil mailUtil;
    private final RedisUtil redisUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int AUTH_CODE_LENGTH = 10;
    private static final char[] AUTH_CODE_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final long AUTH_CODE_TTL = 5L;
    private static final long VERIFIED_TTL = 10L;
    private static final String SIGNUP_EMAIL_AUTH_KEY_PREFIX = "signup:email:auth:";
    private static final String SIGNUP_EMAIL_VERIFIED_KEY_PREFIX = "signup:email:verified:";

    private static final long SIGNIN_AUTH_CODE_TTL = 5L;     // minutes
    private static final long SIGNIN_VERIFIED_TTL = 10L;
    private static final String SIGNIN_EMAIL_AUTH_KEY_PREFIX = "signin:email:auth:";
    private static final String SIGNIN_EMAIL_VERIFIED_KEY_PREFIX = "signin:email:verified:";

    public void signUp(final SignUpRequest request) {
        verifyEmailVerified(request.email());
        final String encodingPassword = passwordEncoder.encode(request.password());
        memberConvenience.checkIsDuplicateEmail(request.email());

        memberRepository.findByStudentIdAndName(request.studentId(), request.name())
                .ifPresentOrElse(
                        member -> member.updateTeamLeaderInfo(request.email(), encodingPassword),
                        () -> registerNewMember(request.name(), request.studentId(), request.email(), encodingPassword)
                );

        redisUtil.delete(SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + request.email());
    }

    public void signUpEmailAuth(final EmailAuthRequest request) {
        final String email = request.email();
        memberConvenience.validatePusanDomain(email);

        final String code = generateRandomAuthCode();

        redisUtil.set(SIGNUP_EMAIL_AUTH_KEY_PREFIX + email, code, AUTH_CODE_TTL, TimeUnit.MINUTES);
        sendAuthCodeMail(email, code);
    }

    public void confirmSignUpEmailAuth(final EmailAuthConfirmRequest request) {
        final String email = request.email();
        validateAuthCode(email, request.authCode());

        redisUtil.delete(SIGNUP_EMAIL_AUTH_KEY_PREFIX + email);
        redisUtil.set(SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + email, "true", VERIFIED_TTL, TimeUnit.MINUTES);
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
        memberConvenience.validateExistMemberByEmail(request.email());

        final String code = generateRandomAuthCode();

        redisUtil.set(SIGNUP_EMAIL_AUTH_KEY_PREFIX + email, code, SIGNIN_AUTH_CODE_TTL, TimeUnit.MINUTES);
        sendAuthCodeMail(email, code);
    }

    private void verifyEmailVerified(final String email) {
        if (redisUtil.get(SIGNUP_EMAIL_VERIFIED_KEY_PREFIX + email) == null) {
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
        char[] buf = new char[AUTH_CODE_LENGTH];
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

    private void validateAuthCode(final String email, final String inputCode) {
        Optional.ofNullable(redisUtil.get(SIGNUP_EMAIL_AUTH_KEY_PREFIX + email))
                .map(code -> {
                    if (!code.equals(inputCode)) {
                        throw new MemberException(EMAIL_AUTH_CODE_MISMATCH);
                    }
                    return code;
                })
                .orElseThrow(() -> new MemberException(EMAIL_AUTH_CODE_EXPIRED));
    }

    private void checkCorrectPassword(final String savePassword, final String inputPassword) {
        if (!passwordEncoder.matches(inputPassword, savePassword)) {
            throw new MemberException(CANNOT_MATCH_PASSWORD);
        }
    }

}
