package com.opus.opus.member.application;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.dto.request.ForceDeleteMemberRequest;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberWithdrawalReason;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.domain.dao.MemberWithdrawalHistoryRepository;
import com.opus.opus.modules.member.exception.MemberException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MemberAdminConcurrencyTest extends IntegrationTest {

    private static final int TIMEOUT_SECONDS = 10;

    @Autowired
    private MemberCommandService memberCommandService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> committedMemberIds = new ArrayList<>();

    @AfterEach
    void 테스트에서_커밋된_회원과_탈퇴_이력을_정리한다() {
        memberWithdrawalHistoryRepository.deleteAllInBatch();
        for (final Long memberId : committedMemberIds) {
            jdbcTemplate.update("DELETE FROM member_roles WHERE member_id = ?", memberId);
            jdbcTemplate.update("DELETE FROM member WHERE id = ?", memberId);
        }
    }

    @Test
    @DisplayName("[성공] 관리자 둘이 동시에 서로를 강제 탈퇴해도 활성 관리자가 한 명 남는다.")
    void 관리자_둘이_동시에_서로를_강제_탈퇴해도_활성_관리자가_한_명_남는다() throws Exception {
        // given
        final Member firstAdmin = saveAdmin(901);
        final Member secondAdmin = saveAdmin(902);
        final CountDownLatch readyLatch = new CountDownLatch(2);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            // when
            final Future<Optional<MemberException>> firstResult = executorService.submit(
                    withdrawOtherAdmin(firstAdmin.getId(), secondAdmin.getId(), readyLatch, startLatch));
            final Future<Optional<MemberException>> secondResult = executorService.submit(
                    withdrawOtherAdmin(secondAdmin.getId(), firstAdmin.getId(), readyLatch, startLatch));
            assertThat(readyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            final List<MemberException> failures = List.of(
                            firstResult.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                            secondResult.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).stream()
                    .flatMap(Optional::stream)
                    .toList();

            // then
            assertThat(failures).singleElement()
                    .extracting(MemberException::getMessage)
                    .isEqualTo(NOT_ADMIN.errorMessage());
            assertThat(memberRepository.findAllById(List.of(firstAdmin.getId(), secondAdmin.getId()))).hasSize(1);
        } finally {
            executorService.shutdownNow();
        }
    }

    private Member saveAdmin(final int number) {
        final Member admin = memberRepository.save(MemberFixture.createMemberWithRole("관리자", number, ROLE_관리자));
        committedMemberIds.add(admin.getId());
        return admin;
    }

    private Callable<Optional<MemberException>> withdrawOtherAdmin(final Long adminId, final Long memberId,
                                                                   final CountDownLatch readyLatch,
                                                                   final CountDownLatch startLatch) {
        final ForceDeleteMemberRequest request =
                new ForceDeleteMemberRequest(MemberWithdrawalReason.OPERATIONAL_CLEANUP, null);
        return () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                memberCommandService.withdrawByAdmin(adminId, memberId, request);
                return Optional.empty();
            } catch (final MemberException e) {
                return Optional.of(e);
            }
        };
    }
}
