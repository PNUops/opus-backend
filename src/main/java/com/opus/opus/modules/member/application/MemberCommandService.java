package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_VERIFIED_EMAIL_AUTH;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.global.util.MailUtil;
import com.opus.opus.global.util.RedisUtil;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.util.Set;
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

    private static final String SIGNUP_EMAIL_AUTH_KEY_PREFIX = "signup:email:auth:";
    private static final String SIGNUP_EMAIL_VERIFIED_KEY_PREFIX = "signup:email:verified:";

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
}
