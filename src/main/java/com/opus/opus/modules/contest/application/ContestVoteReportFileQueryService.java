package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteReportResponse;
import com.opus.opus.modules.contest.application.dto.response.HourlyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.VoteUsageResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.file.application.FilePdfQueryService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

// 템플릿 렌더링·PDF 변환(CPU 작업)이 DB 커넥션을 점유하지 않도록 클래스 레벨 트랜잭션을 두지 않는다.
// 조회는 ContestConvenience와 ContestQueryService가 각자의 읽기 전용 트랜잭션에서 끝낸다.
@Service
@RequiredArgsConstructor
public class ContestVoteReportFileQueryService {

    private static final String VOTE_REPORT_TEMPLATE = "pdf/vote-report";
    private static final int MAX_CHART_TICK_COUNT = 12;
    private static final List<Integer> CHART_TICK_STEP_HOURS = List.of(1, 2, 3, 6, 12, 24, 48, 72, 168);
    private static final List<Double> NICE_AXIS_FACTORS = List.of(1.0, 2.0, 5.0, 10.0);
    private static final long MIN_AXIS_MAX = 10L;

    private final ContestQueryService contestQueryService;
    private final FilePdfQueryService filePdfQueryService;

    private final ContestConvenience contestConvenience;

    private final TemplateEngine templateEngine;

    public FileDownload generateVoteReportPdf(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        validateVoteEnded(contest);

        final ContestVoteReportResponse report = contestQueryService.getVoteReport(contest);
        final byte[] pdf = filePdfQueryService.generatePdf(renderVoteReportHtml(report));

        return new FileDownload(new ByteArrayResource(pdf), generatePdfFileName(contest.getContestName()),
                MediaType.APPLICATION_PDF_VALUE);
    }

    private void validateVoteEnded(final Contest contest) {
        if (!contest.isVoteEnded()) {
            throw new ContestException(ContestExceptionType.VOTE_NOT_ENDED);
        }
    }

    private String renderVoteReportHtml(final ContestVoteReportResponse report) {
        final int tickStepHours = calculateTickStepHours(report.hourlyVotes().size());

        final Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("fontFamily", filePdfQueryService.getFontFamily());
        context.setVariable("fullVoteUsageVoterCount", countFullVoteUsageVoters(report));
        context.setVariable("chartMaxVotes", calculateNiceAxisMax(findMaxHourlyVoteCount(report)));
        context.setVariable("chartMaxVoters", calculateNiceAxisMax(report.totalVoters()));
        context.setVariable("tickStepHours", tickStepHours);
        context.setVariable("tickIndexes", extractTickIndexes(report.hourlyVotes(), tickStepHours));
        return templateEngine.process(VOTE_REPORT_TEMPLATE, context);
    }

    // 1인 최대 투표 수를 모두 사용한 투표자 수 (최대 투표 수가 0이면 모두 사용한 투표자도 없다)
    private long countFullVoteUsageVoters(final ContestVoteReportResponse report) {
        if (report.maxVotesLimit() <= 0) {
            return 0L;
        }
        return report.voteUsages().stream()
                .filter(voteUsage -> voteUsage.usedVotes() >= report.maxVotesLimit())
                .mapToLong(VoteUsageResponse::voterCount)
                .sum();
    }

    private long findMaxHourlyVoteCount(final ContestVoteReportResponse report) {
        return report.hourlyVotes().stream()
                .mapToLong(HourlyVoteResponse::voteCount)
                .max()
                .orElse(0L);
    }

    // 눈금 라벨이 MAX_CHART_TICK_COUNT개 이하가 되는 가장 작은 시간 간격
    private int calculateTickStepHours(final int hourCount) {
        return CHART_TICK_STEP_HOURS.stream()
                .filter(stepHours -> hourCount / stepHours <= MAX_CHART_TICK_COUNT)
                .findFirst()
                .orElse(CHART_TICK_STEP_HOURS.get(CHART_TICK_STEP_HOURS.size() - 1));
    }

    // 시간대 목록 중 눈금 라벨을 붙일 위치 (하루 이상 간격이면 자정 기준)
    private Set<Integer> extractTickIndexes(final List<HourlyVoteResponse> hourlyVotes, final int tickStepHours) {
        return IntStream.range(0, hourlyVotes.size())
                .filter(index -> isTickHour(hourlyVotes.get(index), tickStepHours))
                .boxed()
                .collect(Collectors.toSet());
    }

    private boolean isTickHour(final HourlyVoteResponse hourlyVote, final int tickStepHours) {
        final int hourOfDay = hourlyVote.hour().getHour();
        if (tickStepHours < 24) {
            return hourOfDay % tickStepHours == 0;
        }
        return hourOfDay == 0 && hourlyVote.hour().toLocalDate().toEpochDay() % (tickStepHours / 24) == 0;
    }

    // 차트 축 최대값을 1·2·5·10 × 10^n 중 value 이상인 가장 작은 값으로 올린다. (예: 121 → 200, 339 → 500)
    // 축 중간 눈금(최대값의 절반)이 정수가 되도록 최소값은 10으로 둔다.
    private long calculateNiceAxisMax(final long value) {
        if (value <= MIN_AXIS_MAX) {
            return MIN_AXIS_MAX;
        }
        final double magnitude = Math.pow(10, Math.floor(Math.log10(value)));
        return NICE_AXIS_FACTORS.stream()
                .map(factor -> (long) Math.ceil(factor * magnitude))
                .filter(axisMax -> axisMax >= value)
                .findFirst()
                .orElse((long) (10 * magnitude));
    }

    private String generatePdfFileName(final String contestName) {
        final String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "%s_투표결과_%s.pdf".formatted(contestName.replaceAll("\\s+", ""), date);
    }
}
