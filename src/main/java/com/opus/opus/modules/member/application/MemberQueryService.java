package com.opus.opus.modules.member.application;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.application.dto.response.MyVoteResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberConvenience memberConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestAwardConvenience contestAwardConvenience;
    private final TeamVoteConvenience teamVoteConvenience;

    public EmailFindResponse getMyEmail(final String studentId) {
        final Member member = memberConvenience.getValidateExistMemberByStudentId(studentId);
        return new EmailFindResponse(member.getEmail());
    }

    public List<MyProjectResponse> getMyProjects(final Long memberId) {
        final List<Team> teams = teamMemberConvenience.getValidateExistTeams(memberId);

        final List<Long> contestIds = teams.stream().map(Team::getContestId).distinct().toList();
        final Map<Long, String> contestNameMap = buildContestNameMap(contestIds);
        final Map<Long, String> trackNameMap = buildTrackNameMap(contestIds);
        final Map<Long, List<ContestAward>> awardMap = buildAwardMap(teams);

        return teams.stream()
                .map(team -> MyProjectResponse.of(
                        team,
                        contestNameMap.get(team.getContestId()),
                        trackNameMap.get(team.getTrackId()),
                        awardMap.getOrDefault(team.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private Map<Long, String> buildContestNameMap(final List<Long> contestIds) {
        return contestConvenience.getValidateContests(contestIds).stream()
                .collect(Collectors.toMap(Contest::getId, Contest::getContestName));
    }

    private Map<Long, String> buildTrackNameMap(final List<Long> contestIds) {
        return contestTrackConvenience.getValidateExistTracks(contestIds).stream()
                .collect(Collectors.toMap(ContestTrack::getId, ContestTrack::getTrackName));
    }

    public List<MyVoteResponse> getMyVotes(final Long memberId) {
        final List<TeamVote> votes = teamVoteConvenience.getCurrentVotes(memberId);

        final List<Team> votedTeams = votes.stream().map(TeamVote::getTeam).toList();
        final List<Long> contestIds = votedTeams.stream().map(Team::getContestId).distinct().toList();

        final Map<Long, Contest> contestMap = contestConvenience.getValidateContests(contestIds).stream()
                .collect(Collectors.toMap(Contest::getId, contest -> contest));

        return votedTeams.stream()
                .filter(team -> {
                    final Contest contest = contestMap.get(team.getContestId());
                    return contest != null && contest.isVotingPeriod();
                })
                .map(team -> MyVoteResponse.of(team, contestMap.get(team.getContestId()).getContestName()))
                .toList();
    }

    private Map<Long, List<ContestAward>> buildAwardMap(final List<Team> teams) {
        final List<Long> teamIds = teams.stream().map(Team::getId).toList();
        return contestAwardConvenience.getAwardsByTeamIds(teamIds);
    }
}
