package com.opus.opus.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_INVITED;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVALID_INVITATION_ROLE;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
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
import com.opus.opus.modules.invitation.application.InvitationCommandService;
import com.opus.opus.modules.invitation.application.InvitationTokenProvider;
import com.opus.opus.modules.invitation.application.dto.request.InvitationCreateRequest;
import com.opus.opus.modules.invitation.application.dto.response.InvitationCreateResponse;
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

public class InvitationCommandServiceTest extends IntegrationTest {

    private static final String INVITED_EMAIL = "mentor@example.com";

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

    private Contest contest;

    @BeforeEach
    void setUp() {
        final ContestCategory contestCategory = contestCategoryRepository.save(
                ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(contestCategory.getId()));
    }

    @Test
    @DisplayName("[성공] 관리자는 외부멘토 초대 링크를 발급할 수 있다.")
    void 관리자는_외부멘토_초대_링크를_발급할_수_있다() {
        final InvitationCreateRequest request = new InvitationCreateRequest(
                contest.getId(), "외부멘토", "Mentor@Example.com", ROLE_외부멘토);

        final InvitationCreateResponse response = invitationCommandService.createInvitation(1L, request);

        final Invitation invitation = invitationRepository.findById(response.invitationId()).orElseThrow();
        assertThat(invitation.getEmail()).isEqualTo(INVITED_EMAIL);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitationTokenRepository.findByTokenHash(invitationTokenProvider.hash(response.token())))
                .isPresent();
        assertThat(response.expiresAt()).isAfter(LocalDateTime.now().plusHours(47));
    }

    @Test
    @DisplayName("[실패] 외부멘토가 아닌 역할로는 초대 링크를 발급할 수 없다.")
    void 외부멘토가_아닌_역할로는_초대_링크를_발급할_수_없다() {
        final InvitationCreateRequest request = new InvitationCreateRequest(
                contest.getId(), "교수", INVITED_EMAIL, ROLE_교수);

        assertThatThrownBy(() -> invitationCommandService.createInvitation(1L, request))
                .isInstanceOf(InvitationException.class)
                .hasMessage(INVALID_INVITATION_ROLE.errorMessage());
        assertThat(invitationRepository.count()).isZero();
        assertThat(invitationTokenRepository.count()).isZero();
    }

    @Test
    @DisplayName("[실패] 같은 대회에 취소되지 않은 초대가 있으면 초대 링크를 발급할 수 없다.")
    void 같은_대회에_취소되지_않은_초대가_있으면_초대_링크를_발급할_수_없다() {
        invitationRepository.save(InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL));
        final InvitationCreateRequest request = new InvitationCreateRequest(
                contest.getId(), "외부멘토", INVITED_EMAIL, ROLE_외부멘토);

        assertThatThrownBy(() -> invitationCommandService.createInvitation(1L, request))
                .isInstanceOf(InvitationException.class)
                .hasMessage(ALREADY_INVITED.errorMessage());
        assertThat(invitationRepository.count()).isEqualTo(1);
        assertThat(invitationTokenRepository.count()).isZero();
    }

    @Test
    @DisplayName("[성공] 취소된 초대만 있으면 같은 이메일로 다시 초대 링크를 발급할 수 있다.")
    void 취소된_초대만_있으면_같은_이메일로_다시_초대_링크를_발급할_수_있다() {
        final Invitation canceledInvitation = InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL);
        setField(canceledInvitation, "status", InvitationStatus.CANCELED);
        invitationRepository.save(canceledInvitation);
        final InvitationCreateRequest request = new InvitationCreateRequest(
                contest.getId(), "외부멘토", INVITED_EMAIL, ROLE_외부멘토);

        final InvitationCreateResponse response = invitationCommandService.createInvitation(1L, request);

        final Invitation invitation = invitationRepository.findById(response.invitationId()).orElseThrow();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitationRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 다른 대회에는 같은 이메일로 초대 링크를 발급할 수 있다.")
    void 다른_대회에는_같은_이메일로_초대_링크를_발급할_수_있다() {
        final Contest otherContest = contestRepository.save(
                ContestFixture.createContestWithCategoryId(contest.getCategoryId()));
        invitationRepository.save(InvitationFixture.createInvitation(otherContest.getId(), INVITED_EMAIL));
        final InvitationCreateRequest request = new InvitationCreateRequest(
                contest.getId(), "외부멘토", INVITED_EMAIL, ROLE_외부멘토);

        final InvitationCreateResponse response = invitationCommandService.createInvitation(1L, request);

        final Invitation invitation = invitationRepository.findById(response.invitationId()).orElseThrow();
        assertThat(invitation.getContestId()).isEqualTo(contest.getId());
    }
}
