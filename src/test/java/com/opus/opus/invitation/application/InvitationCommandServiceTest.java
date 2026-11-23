package com.opus.opus.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_ACCEPTED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_INVITED;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.CANCELED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.CANNOT_ACCEPT_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.EXPIRED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVALID_INVITATION_ROLE;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVITATION_EMAIL_MISMATCH;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.REPLACED_INVITATION;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_직원;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.invitation.InvitationFixture;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
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
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InvitationCommandServiceTest extends IntegrationTest {

    private static final String INVITED_EMAIL = "mentor@example.com";
    private static final String TOKEN = "invitation-token";

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
    private ContestMemberRepository contestMemberRepository;
    @Autowired
    private MemberRepository memberRepository;

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

    @Test
    @DisplayName("[성공] 초대 이후 가입한 회원은 초대를 수락하면 학생 역할 대신 외부멘토 역할을 받는다.")
    void 초대_이후_가입한_회원은_초대를_수락하면_학생_역할_대신_외부멘토_역할을_받는다() {
        final Invitation invitation = saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        final SignInResponse response = invitationCommandService.acceptInvitation(member.getId(), TOKEN);

        assertThat(response.types()).containsExactly(ROLE_외부멘토);
        assertThat(response.token()).isNotBlank();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getAcceptedMemberId()).isEqualTo(member.getId());
        assertThat(contestMemberRepository.existsByContestIdAndMemberId(contest.getId(), member.getId())).isTrue();
    }

    @Test
    @DisplayName("[성공] 초대 전에 가입한 회원은 초대를 수락해도 기존 역할을 유지한다.")
    void 초대_전에_가입한_회원은_초대를_수락해도_기존_역할을_유지한다() {
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));
        saveInvitationWithToken(LocalDateTime.now().plusHours(1));

        final SignInResponse response = invitationCommandService.acceptInvitation(member.getId(), TOKEN);

        assertThat(response.types()).containsExactlyInAnyOrder(ROLE_학생, ROLE_외부멘토);
    }

    @Test
    @DisplayName("[성공] 이미 수락한 회원이 다시 수락하면 새 토큰을 받는다.")
    void 이미_수락한_회원이_다시_수락하면_새_토큰을_받는다() {
        saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));
        invitationCommandService.acceptInvitation(member.getId(), TOKEN);

        final SignInResponse response = invitationCommandService.acceptInvitation(member.getId(), TOKEN);

        assertThat(response.token()).isNotBlank();
        assertThat(contestMemberRepository.findAllByMemberId(member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("[실패] 취소된 초대는 수락할 수 없다.")
    void 취소된_초대는_수락할_수_없다() {
        final Invitation invitation = saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        setField(invitation, "status", InvitationStatus.CANCELED);
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(CANCELED_INVITATION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 회원이 수락한 초대는 수락할 수 없다.")
    void 다른_회원이_수락한_초대는_수락할_수_없다() {
        final Invitation invitation = saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        invitation.accept(999L, LocalDateTime.now());
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(ALREADY_ACCEPTED_INVITATION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 재발급으로 교체된 링크로는 초대를 수락할 수 없다.")
    void 재발급으로_교체된_링크로는_초대를_수락할_수_없다() {
        final Invitation invitation = saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash("new-token"), LocalDateTime.now().plusHours(1)));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(REPLACED_INVITATION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 만료된 링크로는 초대를 수락할 수 없다.")
    void 만료된_링크로는_초대를_수락할_수_없다() {
        saveInvitationWithToken(LocalDateTime.now().minusMinutes(1));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(EXPIRED_INVITATION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 초대받은 이메일과 다른 계정으로는 초대를 수락할 수 없다.")
    void 초대받은_이메일과_다른_계정으로는_초대를_수락할_수_없다() {
        saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("other@example.com", ROLE_학생));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(INVITATION_EMAIL_MISMATCH.errorMessage());
    }

    @Test
    @DisplayName("[실패] 직원 계정은 초대를 수락할 수 없다.")
    void 직원_계정은_초대를_수락할_수_없다() {
        saveInvitationWithToken(LocalDateTime.now().plusHours(1));
        final Member member = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_직원));

        assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                .isInstanceOf(InvitationException.class)
                .hasMessage(CANNOT_ACCEPT_INVITATION.errorMessage());
    }

    private Invitation saveInvitationWithToken(final LocalDateTime expiresAt) {
        final Invitation invitation = invitationRepository.save(
                InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash(TOKEN), expiresAt));
        return invitation;
    }
}
