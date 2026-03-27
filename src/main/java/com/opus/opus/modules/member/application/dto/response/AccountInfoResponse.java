package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;

public record AccountInfoResponse(

        String name,

        String email,

        String githubUrl,

        Boolean isProfilePublic
) {
    public static AccountInfoResponse from(final Member member) {
        return new AccountInfoResponse(
                member.getName(),
                member.getEmail(),
                member.getGithubUrl(),
                member.getIsProfilePublic()
        );
    }
}
