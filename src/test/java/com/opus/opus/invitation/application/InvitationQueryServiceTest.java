package com.opus.opus.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.NOT_FOUND_INVITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.invitation.InvitationFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.invitation.application.InvitationQueryService;
import com.opus.opus.modules.invitation.application.InvitationTokenProvider;
import com.opus.opus.modules.invitation.application.dto.response.InvitationResponse;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import com.opus.opus.modules.invitation.domain.dao.InvitationRepository;
import com.opus.opus.modules.invitation.domain.dao.InvitationTokenRepository;
import com.opus.opus.modules.invitation.exception.InvitationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InvitationQueryServiceTest extends IntegrationTest {

    private static final String INVITED_EMAIL = "mentor@example.com";
    private static final String TOKEN = "invitation-token";

    @Autowired
    private InvitationQueryService invitationQueryService;

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

    private Contest contest;

    @BeforeEach
    void setUp() {
        final ContestCategory contestCategory = contestCategoryRepository.save(
                ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(contestCategory.getId()));
    }

    @Test
    @DisplayName("[성공] 유효한 토큰으로 초대 정보를 조회할 수 있다.")
    void 유효한_토큰으로_초대_정보를_조회할_수_있다() {
        saveInvitationWithToken(TOKEN, LocalDateTime.now().plusHours(1));

        final InvitationResponse response = invitationQueryService.getInvitation(TOKEN);

        assertThat(response.contestName()).isEqualTo(contest.getContestName());
        assertThat(response.email()).isEqualTo(INVITED_EMAIL);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.serverTime()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 만료된 토큰으로 조회하면 만료 상태를 반환한다.")
    void 만료된_토큰으로_조회하면_만료_상태를_반환한다() {
        saveInvitationWithToken(TOKEN, LocalDateTime.now().minusMinutes(1));

        final InvitationResponse response = invitationQueryService.getInvitation(TOKEN);

        assertThat(response.status()).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("[성공] 재발급으로 교체된 토큰으로 조회하면 교체 상태를 반환한다.")
    void 재발급으로_교체된_토큰으로_조회하면_교체_상태를_반환한다() {
        final Invitation invitation = saveInvitationWithToken(TOKEN, LocalDateTime.now().plusHours(1));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash("new-token"), LocalDateTime.now().plusHours(1)));

        final InvitationResponse response = invitationQueryService.getInvitation(TOKEN);

        assertThat(response.status()).isEqualTo("REPLACED");
    }

    @Test
    @DisplayName("[성공] 취소된 초대는 토큰이 만료되었어도 취소 상태를 반환한다.")
    void 취소된_초대는_토큰이_만료되었어도_취소_상태를_반환한다() {
        final Invitation invitation = saveInvitationWithToken(TOKEN, LocalDateTime.now().minusMinutes(1));
        setField(invitation, "status", InvitationStatus.CANCELED);
        invitationRepository.save(invitation);

        final InvitationResponse response = invitationQueryService.getInvitation(TOKEN);

        assertThat(response.status()).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 토큰으로는 초대 정보를 조회할 수 없다.")
    void 존재하지_않는_토큰으로는_초대_정보를_조회할_수_없다() {
        assertThatThrownBy(() -> invitationQueryService.getInvitation("unknown-token"))
                .isInstanceOf(InvitationException.class)
                .hasMessage(NOT_FOUND_INVITATION.errorMessage());
    }

    private Invitation saveInvitationWithToken(final String token, final LocalDateTime expiresAt) {
        final Invitation invitation = invitationRepository.save(
                InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash(token), expiresAt));
        return invitation;
    }
}
