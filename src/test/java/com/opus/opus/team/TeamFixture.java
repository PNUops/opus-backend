package com.opus.opus.team;

import com.opus.opus.modules.team.domain.Team;

public class TeamFixture {

    public static Team createTeam() {
        return Team.builder()
                .teamName("테스트 팀")
                .projectName("테스트 프로젝트")
                .contestId(1L)
                .itemOrder(1)
                .build();
    }
}
