package com.opus.opus.modules.member.application.convenience;

import static com.opus.opus.modules.member.exception.MemberExceptionType.ALREADY_EXIST_EMAIL;
import static com.opus.opus.modules.member.exception.MemberExceptionType.ALREADY_EXIST_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberConvenience {

    final private MemberRepository memberRepository;

    public Member getValidateExistMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public Member getValidateExistMemberByStudentId(final String studentId) {
        return memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public void validateExistMemberByEmail(final String email) {
        memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    public Member getValidateExistMemberByEmail(final String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
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

}
