package com.opus.opus.modules.member.application;

import com.opus.opus.modules.member.domain.Member;

public enum MemberStatus {
    ACTIVE,
    WITHDRAWN,
    ;

    public static MemberStatus from(final Member member) {
        return Boolean.TRUE.equals(member.getIsDeleted()) ? WITHDRAWN : ACTIVE;
    }
}
