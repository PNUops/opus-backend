package com.opus.opus.modules.member.domain;

import lombok.Getter;

@Getter
public enum MemberRoleType {
    ROLE_회원(1),
    ROLE_관리자(2),
    ;

    private final long id;

    MemberRoleType(final long id) {
        this.id = id;
    }
}
