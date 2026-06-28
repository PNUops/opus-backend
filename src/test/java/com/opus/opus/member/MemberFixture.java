package com.opus.opus.member;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.SocialType;
import java.util.HashSet;
import java.util.Set;

public class MemberFixture {

    public static Member createMemberWithEmailAndRole(final String email, final MemberRoleType role) {
        return createMemberWithEmailAndRoles(email, role);
    }

    public static Member createMemberWithEmailAndRoles(final String email, final MemberRoleType... roles) {
        return Member.generalMember()
                .name("테스트회원")
                .email(email)
                .password("{noop}123456789")
                .studentId(email.substring(0, email.indexOf('@')))
                .roles(new HashSet<>(Set.of(roles)))
                .build();
    }

    public static Member createSocialMember(String email, String socialId) {
        return Member.socialMember()
                .name("소셜회원")
                .email(email)
                .socialType(SocialType.GOOGLE)
                .socialId(socialId)
                .roles(new HashSet<>(Set.of(ROLE_학생)))
                .build();
    }

    public static Member createMemberWithUniqueNum(int number) {
        return Member.generalMember()
                .name("테스트회원" + number)
                .email("example" + number + "@pusan.ac.kr")
                .password("{noop}123456789")
                .studentId("20211234" + number)
                .roles(Set.of(ROLE_학생))
                .build();
    }

    public static Member createMember() {
        return Member.generalMember()
                .name("테스트회원")
                .email("example@pusan.ac.kr")
                .password("{noop}123456789")
                .studentId("202612345")
                .roles(new HashSet<>(Set.of(ROLE_학생)))
                .build();
    }
}
