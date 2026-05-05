package com.opus.opus.modules.member.application.convenience;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;
import static com.opus.opus.modules.member.exception.MemberExceptionType.ALREADY_EXIST_EMAIL;
import static com.opus.opus.modules.member.exception.MemberExceptionType.ALREADY_EXIST_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.MISMATCH_STUDENT_ID_AND_NAME;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberConvenience {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PASSWORD_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    final private MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member getValidateExistMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public Member getValidateExistMemberByStudentId(final String studentId) {
        return memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public Member getValidateExistMemberByEmail(final String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public Optional<Member> findByEmail(final String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByStudentId(final String studentId) {
        return memberRepository.findByStudentId(studentId);
    }

    public void validatePusanDomain(final String email) {
        if (!email.endsWith("@pusan.ac.kr")) {
            throw new MemberException(NOT_PUSAN_UNIVERSITY_EMAIL);
        }
    }

    public void checkIsDuplicateEmail(final String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(ALREADY_EXIST_EMAIL);
        }
    }

    public void checkIsDuplicateStudentId(final String studentId) {
        if (memberRepository.existsByStudentId(studentId)) {
            throw new MemberException(ALREADY_EXIST_STUDENT_ID);
        }
    }

    public List<Member> findAllById(final List<Long> memberIds) {
        return memberRepository.findAllById(memberIds);
    }

    public List<Member> findAllByIdIncludingDeleted(final List<Long> memberIds) {
        return memberRepository.findAllByIdIncludingDeleted(memberIds);
    }

    private void validateNameMatchesStudentId(final String studentId, final String name) {
        memberRepository.findByStudentId(studentId)
                .ifPresent(member -> {
                    if (!member.getName().equals(name)) {
                        throw new MemberException(MISMATCH_STUDENT_ID_AND_NAME);
                    }
                });
    }

    public Member getOrCreateFakeMember(final String studentId, final String name) {
        validateNameMatchesStudentId(studentId, name);

        return memberRepository.findByStudentId(studentId)
                .orElseGet(() -> registerFakeMember(studentId, name));
    }

    private Member registerFakeMember(final String studentId, final String name) {
        final String email = "fake_" + studentId + "@pusan.ac.kr";
        final String randomPassword = generateRandomPassword();

        return memberRepository.save(
                Member.generalMember()
                        .name(name)
                        .studentId(studentId)
                        .email(email)
                        .password(randomPassword)
                        .roles(Set.of(ROLE_회원))
                        .build()
        );
    }

    public Member getOrCreateFakeMember(final String email, final String studentId, final String name) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> registerFakeMemberWithEmailAndStudentId(email, studentId, name));
    }

    private Member registerFakeMemberWithEmailAndStudentId(final String email, final String studentId, final String name) {
        final String randomPassword = generateRandomPassword();

        final Member member = Member.generalMember()
                .name(name)
                .studentId(studentId)
                .email(email)
                .password(randomPassword)
                .roles(Set.of(ROLE_회원))
                .build();
        member.markAsFakeMember();

        return memberRepository.save(member);
    }

    private String generateRandomPassword() {
        final int passwordLength = 32;

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            password.append(PASSWORD_POOL.charAt(SECURE_RANDOM.nextInt(PASSWORD_POOL.length())));
        }

        return passwordEncoder.encode(password.toString());
    }

    public Map<String, Member> findAllByEmailIn(final List<String> emails) {
        return memberRepository.findAllByEmailIn(emails)
                .stream()
                .collect(toMap(Member::getEmail, Function.identity()));
    }

    public Map<String, Member> findAllByStudentIdIn(final List<String> studentIds) {
        return memberRepository.findAllByStudentIdIn(studentIds)
                .stream()
                .collect(toMap(Member::getStudentId, Function.identity()));
    }

    public long countActiveMembers() {
        return memberRepository.countByIsFakeFalse();
    }

    public Map<Long, Member> getMembersByIds(final List<Long> memberIds) {
        return memberRepository.findAllById(memberIds)
                .stream()
                .collect(toMap(
                        Member::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }
}
