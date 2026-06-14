package com.opus.opus.member;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;

import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.StaffInfo;

public class StaffInfoFixture {

    public static StaffInfo createStaffInfo(final String name, final String email, final MemberRoleType role) {
        return StaffInfo.builder()
                .name(name)
                .email(email)
                .role(role)
                .build();
    }

    public static StaffInfo createStaffInfo() {
        return StaffInfo.builder()
                .name("교직원")
                .email("staff@pusan.ac.kr")
                .role(ROLE_교수)
                .build();
    }
}
