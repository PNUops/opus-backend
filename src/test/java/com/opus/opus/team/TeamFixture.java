package com.opus.opus.team;

import com.opus.opus.modules.team.domain.Team;
import java.util.ArrayList;

public class TeamFixture {

    private static final Long DEFAULT_CONTEST_ID = 1L;

    public static Team createTeam() {
        return createTeamWithContestId(DEFAULT_CONTEST_ID);
    }

    public static Team createTeamWithContestId(final Long contestId) {
        return Team.builder()
                .teamName("팀 옵스")
                .projectName("옵스 프로젝트")
                .professorName("김교수")
                .overview("이 프로젝트는 옵스 프로젝트입니다.")
                .githubPath("http://github.com/example")
                .productionPath("http://production.example.com")
                .youTubePath("http://youtube.com/example")
                .contestId(contestId)
                .trackId(1L)
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build();
    }

    public static Team createTeamWithContestIdAndTeamName(final Long contestId, final String teamName) {
        return Team.builder()
                .teamName(teamName)
                .projectName("옵스 프로젝트")
                .professorName("김교수")
                .overview("이 프로젝트는 옵스 프로젝트입니다.")
                .githubPath("http://github.com/example")
                .productionPath("http://production.example.com")
                .youTubePath("http://youtube.com/example")
                .contestId(contestId)
                .trackId(1L)
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build();
    }

    public static Team createTeamWithContestIdAndItemOrder(final Long contestId, final int itemOrder) {
        return Team.builder()
                .teamName("팀 옵스")
                .projectName("옵스 프로젝트")
                .professorName("김교수")
                .overview("이 프로젝트는 옵스 프로젝트입니다.")
                .githubPath("http://github.com/example")
                .productionPath("http://production.example.com")
                .youTubePath("http://youtube.com/example")
                .contestId(contestId)
                .trackId(1L)
                .itemOrder(itemOrder)
                .teamMembers(new ArrayList<>())
                .build();
    }

    public static Team createSubmittedTeamWithContestId(final Long contestId) {
        Team team = Team.builder()
                .contestId(contestId)
                .teamName("제출완료팀")
                .projectName("제출완료 프로젝트")
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build();
        team.submit();
        return team;
    }
}
