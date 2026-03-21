package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestFixture.createContest;
import static com.opus.opus.contest.ContestSortFixture.createContestSort;
import static com.opus.opus.modules.contest.domain.SortType.ASC;
import static com.opus.opus.modules.contest.domain.SortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_TEAM_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_CONTEST_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.EMPTY_TEAM_DATA;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_FILE_FORMAT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_END_PRECEDE_VOTE_START;
import static com.opus.opus.modules.contest.exception.ContestTemplateExceptionType.NOT_FOUND_TEMPLATE;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndItemOrder;
import static com.opus.opus.modules.team.exception.TeamExceptionType.FAILED_TO_VALIDATE_BULK_TEAMS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestExcelFixture;
import com.opus.opus.contest.ContestTemplateFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkUploadResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestTemplateException;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestCommandServiceTest extends IntegrationTest {

    private static final Integer MAX_VOTES_LIMIT = 5;
    @Autowired
    private ContestCommandService contestCommandService;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestSortRepository contestSortRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ContestTemplateRepository contestTemplateRepository;
    private Contest contest;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(createContest());
        contestSortRepository.save(createContestSort(contest));
    }

    @Test
    @DisplayName("[성공] 투표 기간 수정 시 시작일과 종료일이 정상적으로 업데이트된다.")
    void 투표_기간_수정_시_시작일과_종료일이_정상적으로_업데이트된다() {
        // given
        final LocalDateTime originalStartAt = contest.getVoteStartAt();
        final LocalDateTime originalEndAt = contest.getVoteEndAt();

        final LocalDateTime newStartAt = LocalDateTime.now().plusDays(1);
        final LocalDateTime newEndAt = LocalDateTime.now().plusDays(5);
        final VoteUpdateRequest request = new VoteUpdateRequest(newStartAt, newEndAt);

        // when
        contestCommandService.updateVotePeriod(contest.getId(), request);

        // then
        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getVoteStartAt()).isNotEqualTo(originalStartAt);
        assertThat(updatedContest.getVoteEndAt()).isNotEqualTo(originalEndAt);

        assertThat(updatedContest.getVoteStartAt()).isEqualTo(newStartAt);
        assertThat(updatedContest.getVoteEndAt()).isEqualTo(newEndAt);
    }

    @Test
    @DisplayName("[실패] 투표 종료일이 시작일보다 앞서면 예외가 발생한다.")
    void 투표_종료일이_시작일보다_앞서면_예외가_발생한다() {
        final LocalDateTime startAt = LocalDateTime.now().plusDays(5);
        final LocalDateTime endAt = LocalDateTime.now().plusDays(1);
        final VoteUpdateRequest request = new VoteUpdateRequest(startAt, endAt);

        assertThatThrownBy(() -> {
            contestCommandService.updateVotePeriod(contest.getId(), request);
        })
                .isInstanceOf(ContestException.class)
                .hasMessage(VOTE_END_PRECEDE_VOTE_START.errorMessage());
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수가 정상적으로 설정된다.")
    void 최대_투표_개수가_정상적으로_설정된다() {
        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수는 설정할 수 없다.")
    void 존재하지_않는_대회의_최대_투표_개수는_설정할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> {
            contestCommandService.updateMaxVotesLimit(invalidContestId, MAX_VOTES_LIMIT);
        }).isInstanceOf(ContestException.class).hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 진행 중에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_진행_중에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.minusDays(1), now.plusDays(1));

        assertThatThrownBy(() -> {
            contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);
        }).isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_DURING_VOTING_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[성공] 투표 시작 전에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_시작_전에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.plusDays(1), now.plusDays(2));

        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }

    @Test
    @DisplayName("[성공] 투표 종료 후에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_종료_후에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.minusDays(2), now.minusDays(1));
        assertThat(contest.getMaxVotesLimit()).isEqualTo(0); // 변경 전 값 검증

        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow(); // 변경 후 값 검증
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }

    @Test
    @DisplayName("[성공] 대회 정렬 설정 변경을 하면 설정이 변경된다.")
    void 대회_정렬_설정_변경을_하면_설정이_변경된다() {
        final ContestSortRequest request = new ContestSortRequest(ASC);

        contestCommandService.updateContestSort(contest.getId(), request);

        final ContestSort changedContestSort = contestSortRepository.findByContestId(contest.getId()).orElseThrow();
        assertThat(changedContestSort.getMode()).isEqualTo(ASC);
    }

    @Test
    @DisplayName("[성공] 팀 수동 정렬을 하면 정렬 순서가 바뀐다.")
    void 팀_수동_정렬을_하면_정렬_순서가_바뀐다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        contestCommandService.updateContestSortCustom(contest.getId(), requests);

        assertThat(teamOne.getItemOrder()).isEqualTo(2);
        assertThat(teamTwo.getItemOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] CUSTOM모드가 아니라면 수동 정렬은 실패한다.")
    void CUSTOM모드가_아니라면_수동_정렬은_실패한다() {
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(ONLY_CUSTOM_MODE_CAN_CHANGE.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 teamId가 있다면 수동 정렬은 실패한다.")
    void 중복_teamId가_있다면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamOne.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(DUPLICATE_TEAM_ID_IN_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 itemOrder가 있다면 수동 정렬은 실패한다.")
    void 중복_itemOrder가_있다면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 1),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 list size가 팀 개수와 다르면 수동 정렬은 실패한다.")
    void 요청받은_list_size가_팀_개수와_다르면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 3));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(INVALID_CONTEST_SORT_CUSTOM_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 itemOrder가 팀 개수를 넘어가면 수동 정렬은 실패한다.")
    void 요청받은_itemOrder가_팀_개수를_넘어가면_수동_정렬은_실패한다() {
        final int invalidItemOrder = 99;
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 1),
                new ContestSortCustomRequest(teamTwo.getId(), invalidItemOrder));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(INVALID_ITEM_ORDER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 대회 템플릿을 수정한다.")
    void 대회_템플릿을_수정한다() {
        // given
        contestTemplateRepository.save(ContestTemplateFixture.createContestTemplate(contest));
        final ContestTemplateRequest request = new ContestTemplateRequest(
                false, false, false, false, false, false,
                false, false, false, false, false, false
        );

        // when
        contestCommandService.updateContestTemplate(contest.getId(), request);

        // then
        final ContestTemplate updatedTemplate = contestTemplateRepository.findByContestId(contest.getId())
                .orElseThrow();
        assertThat(updatedTemplate.getTrackRequired()).isFalse();
        assertThat(updatedTemplate.getProjectNameRequired()).isFalse();
        assertThat(updatedTemplate.getTeamNameRequired()).isFalse();
        assertThat(updatedTemplate.getLeaderRequired()).isFalse();
        assertThat(updatedTemplate.getTeamMembersRequired()).isFalse();
        assertThat(updatedTemplate.getProfessorRequired()).isFalse();
        assertThat(updatedTemplate.getGithubPathRequired()).isFalse();
        assertThat(updatedTemplate.getYouTubePathRequired()).isFalse();
        assertThat(updatedTemplate.getProductionPathRequired()).isFalse();
        assertThat(updatedTemplate.getOverviewRequired()).isFalse();
        assertThat(updatedTemplate.getPosterRequired()).isFalse();
        assertThat(updatedTemplate.getImagesRequired()).isFalse();
    }

    @Test
    @DisplayName("[실패] 대회 템플릿 정보가 존재하지 않으면 수정에 실패한다.")
    void 대회_템플릿_정보가_존재하지_않으면_수정에_실패한다() {
        final ContestTemplateRequest request = new ContestTemplateRequest(
                false, false, false, false, false, false,
                false, false, false, false, false, false
        );

        assertThatThrownBy(() -> {
            contestCommandService.updateContestTemplate(contest.getId(), request);
        }).isInstanceOf(ContestTemplateException.class).hasMessage(NOT_FOUND_TEMPLATE.errorMessage());
    }

    @Test
    @DisplayName("[성공] 창의융합 카테고리로 템플릿을 생성한다.")
    void 창의융합_카테고리로_템플릿을_생성한다() {
        // given
        final String categoryName = "창의융합공학";

        // when
        contestCommandService.createTemplate(contest, categoryName);

        // then
        final ContestTemplate template = contestTemplateRepository.findByContestId(contest.getId()).orElseThrow();
        assertThat(template.getTrackRequired()).isTrue();
        assertThat(template.getProfessorRequired()).isFalse();
        assertThat(template.getYouTubePathRequired()).isFalse();
    }

    @Test
    @DisplayName("[성공] 캡스톤 카테고리로 템플릿을 생성한다.")
    void 캡스톤_카테고리로_템플릿을_생성한다() {
        // given
        final String categoryName = "캡스톤디자인";

        // when
        contestCommandService.createTemplate(contest, categoryName);

        // then
        final ContestTemplate template = contestTemplateRepository.findByContestId(contest.getId()).orElseThrow();
        assertThat(template.getTrackRequired()).isTrue();
        assertThat(template.getProfessorRequired()).isTrue();
        assertThat(template.getYouTubePathRequired()).isTrue();
        assertThat(template.getPosterRequired()).isFalse();
    }

    @Test
    @DisplayName("[성공] 기본 카테고리로 템플릿을 생성한다.")
    void 기본_카테고리로_템플릿을_생성한다() {
        // given
        final String categoryName = "기타";

        // when
        contestCommandService.createTemplate(contest, categoryName);

        // then
        final ContestTemplate template = contestTemplateRepository.findByContestId(contest.getId()).orElseThrow();
        assertThat(template.getTrackRequired()).isFalse();
        assertThat(template.getProjectNameRequired()).isFalse();
        assertThat(template.getImagesRequired()).isFalse();
    }

    @Test
    @DisplayName("[성공] 엑셀 파일로 팀을 일괄 등록한다.")
    void 엑셀_파일로_팀을_일괄_등록한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"알파팀", "알파프로젝트", "김철수", "", "2021001", "", "kim@pusan.ac.kr", ""},
                new String[]{"베타팀", "베타프로젝트", "이영희", "", "2021002", "", "lee@pusan.ac.kr", ""}
        );

        final TeamBulkUploadResponse response = contestCommandService.bulkUploadTeams(contest.getId(), file);

        assertThat(response.teamCount()).isEqualTo(2);
        assertThat(response.teams()).hasSize(2);
        assertThat(response.teams().get(0).teamName()).isEqualTo("알파팀");
        assertThat(response.teams().get(1).teamName()).isEqualTo("베타팀");
    }

    @Test
    @DisplayName("[성공] 팀장과 팀원이 포함된 엑셀 파일로 팀을 일괄 등록한다.")
    void 팀장과_팀원이_포함된_팀을_일괄_등록한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"감마팀", "감마프로젝트", "김리더", "이팀원, 박팀원", "2021010", "2021011, 2021012", "leader@pusan.ac.kr", "mem1@pusan.ac.kr, mem2@pusan.ac.kr"}
        );

        final TeamBulkUploadResponse response = contestCommandService.bulkUploadTeams(contest.getId(), file);

        assertThat(response.teamCount()).isEqualTo(1);
        assertThat(response.teams().get(0).teamName()).isEqualTo("감마팀");
    }

    @Test
    @DisplayName("[실패] 잘못된 파일 형식이면 예외가 발생한다.")
    void 잘못된_파일_형식이면_예외가_발생한다() {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "teams.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes()
        );

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(contest.getId(), file))
                .isInstanceOf(ContestException.class)
                .hasMessage(INVALID_FILE_FORMAT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 빈 엑셀 파일이면 예외가 발생한다.")
    void 빈_엑셀_파일이면_예외가_발생한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile();

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(contest.getId(), file))
                .isInstanceOf(ContestException.class)
                .hasMessage(EMPTY_TEAM_DATA.errorMessage());
    }

    @Test
    @DisplayName("[실패] 유효성 검사 실패 시 TeamException이 발생한다.")
    void 유효성_검사_실패_시_TeamException이_발생한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"", "프로젝트", "김리더", "", "2021020", "", "test@pusan.ac.kr", ""} // 팀 이름 누락
        );

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(contest.getId(), file))
                .isInstanceOf(TeamException.class)
                .hasMessage(FAILED_TO_VALIDATE_BULK_TEAMS.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이메일 도메인이 올바르지 않으면 유효성 검사에 실패한다.")
    void 이메일_도메인이_올바르지_않으면_유효성_검사에_실패한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"테스트팀", "프로젝트", "김리더", "", "2021030", "", "test@gmail.com", ""}
        );

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(contest.getId(), file))
                .isInstanceOf(TeamException.class)
                .hasMessage(FAILED_TO_VALIDATE_BULK_TEAMS.errorMessage());
    }

    @Test
    @DisplayName("[실패] 파일 내 학번이 중복되면 유효성 검사에 실패한다.")
    void 파일_내_학번이_중복되면_유효성_검사에_실패한다() throws Exception {
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"팀A", "프로젝트A", "김리더", "", "2021040", "", "a@pusan.ac.kr", ""},
                new String[]{"팀B", "프로젝트B", "이리더", "", "2021040", "", "b@pusan.ac.kr", ""}
        );

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(contest.getId(), file))
                .isInstanceOf(TeamException.class)
                .hasMessage(FAILED_TO_VALIDATE_BULK_TEAMS.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에 팀 일괄 등록 시 예외가 발생한다.")
    void 존재하지_않는_대회에_팀_일괄_등록_시_예외가_발생한다() throws Exception {
        final Long invalidContestId = 999L;
        final MockMultipartFile file = ContestExcelFixture.createExcelFile(
                new String[]{"테스트팀", "프로젝트", "김리더", "", "2021050", "", "test@pusan.ac.kr", ""}
        );

        assertThatThrownBy(() -> contestCommandService.bulkUploadTeams(invalidContestId, file))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }
}
