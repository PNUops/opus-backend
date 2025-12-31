package com.opus.opus.modules.contest.domain;

import lombok.Getter;

@Getter
public enum ContestTeamTemplateFieldType {
    REQUIRED(1),
    OPTIONAL(2),
    HIDDEN(3),
    ;

    private final long id;

    ContestTeamTemplateFieldType(final long id) {
        this.id = id;
    }
}

