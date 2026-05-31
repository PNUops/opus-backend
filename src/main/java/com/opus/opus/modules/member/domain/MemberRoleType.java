package com.opus.opus.modules.member.domain;

import lombok.Getter;

@Getter
public enum MemberRoleType {
    ROLE_학생(1),
    ROLE_관리자(2),
    ROLE_교수(3),
    ROLE_직원(4),
    ROLE_외부멘토(5),
    ;

    private final long id;

    MemberRoleType(final long id) {
        this.id = id;
    }
}
