package com.opus.opus.member;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_회원;

import com.opus.opus.modules.member.domain.Member;
import java.util.Set;

public class MemberFixture {

    public static Member createMember() {
        return Member.builder()
                .name("이옵스")
                .email("example@pusan.ac.kr")
                .password("{noop}123456789")
                .studentId("202612345")
                .roles(Set.of(ROLE_회원))
                .build();
    }
}
