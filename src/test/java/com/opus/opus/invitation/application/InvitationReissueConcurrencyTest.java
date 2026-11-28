package com.opus.opus.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.TOO_FREQUENT_REISSUE;
import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.invitation.InvitationFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.invitation.application.InvitationCommandService;
import com.opus.opus.modules.invitation.application.InvitationTokenProvider;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.dao.InvitationRepository;
import com.opus.opus.modules.invitation.domain.dao.InvitationTokenRepository;
import com.opus.opus.modules.invitation.exception.InvitationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InvitationReissueConcurrencyTest extends IntegrationTest {

    private static final String INVITED_EMAIL = "mentor@example.com";
    private static final String TOKEN = "invitation-token";
    private static final Long ADMIN_ID = 1L;
    private static final int REQUEST_COUNT = 2;

    @Autowired
    private InvitationCommandService invitationCommandService;

    @Autowired
    private InvitationTokenProvider invitationTokenProvider;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationTokenRepository invitationTokenRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ContestCategoryRepository contestCategoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ContestCategory contestCategory;
    private Contest contest;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        contestCategory = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(contestCategory.getId()));
        invitation = invitationRepository.save(InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash(TOKEN), LocalDateTime.now().plusHours(1)));
        jdbcTemplate.update("UPDATE invitation_token SET created_at = ? WHERE invitation_id = ?",
                LocalDateTime.now().minusMinutes(1), invitation.getId());
    }

    @AfterEach
    void 테스트에서_커밋된_초대를_정리한다() {
        jdbcTemplate.update("DELETE FROM invitation_token WHERE invitation_id = ?", invitation.getId());
        jdbcTemplate.update("DELETE FROM invitation WHERE id = ?", invitation.getId());
        jdbcTemplate.update("DELETE FROM contest WHERE id = ?", contest.getId());
        jdbcTemplate.update("DELETE FROM contest_category WHERE id = ?", contestCategory.getId());
    }

    @Test
    @DisplayName("[성공] 같은 초대를 동시에 재발급하면 한 번만 발급된다.")
    void 같은_초대를_동시에_재발급하면_한_번만_발급된다() throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            results.add(executorService.submit(() -> {
                startLatch.await();
                try {
                    invitationCommandService.reissueInvitation(ADMIN_ID, invitation.getId());
                    return "SUCCESS";
                } catch (final InvitationException e) {
                    return e.getMessage();
                }
            }));
        }

        startLatch.countDown();
        final List<String> messages = new ArrayList<>();
        for (final Future<String> result : results) {
            messages.add(result.get());
        }
        executorService.shutdown();

        assertThat(messages).containsExactlyInAnyOrder("SUCCESS", TOO_FREQUENT_REISSUE.errorMessage());
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM invitation_token WHERE invitation_id = ?",
                Long.class, invitation.getId())).isEqualTo(2L);
    }
}
