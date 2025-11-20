package com.opus.opus.modules.team.domain;

import lombok.Getter;

@Getter
public enum TeamMemberRoleType {

    ROLE_팀장(1),
    ROLE_팀원(2),
    ;

    private final long id;

    TeamMemberRoleType(final long id) {
        this.id = id;
    }
}
