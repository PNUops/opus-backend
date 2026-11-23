package com.opus.opus.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.CANNOT_ACCEPT_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.REPLACED_INVITATION;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_직원;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.invitation.InvitationFixture;
import com.opus.opus.member.MemberFixture;
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
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InvitationAcceptConcurrencyTest extends IntegrationTest {

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
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ContestCategory contestCategory;
    private Contest contest;
    private Invitation invitation;
    private Member member;
    private TransactionTemplate otherTransaction;

    @BeforeEach
    void setUp() {
        contestCategory = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(contestCategory.getId()));
        invitation = invitationRepository.save(InvitationFixture.createInvitation(contest.getId(), INVITED_EMAIL));
        invitationTokenRepository.save(InvitationFixture.createInvitationToken(
                invitation, invitationTokenProvider.hash(TOKEN), LocalDateTime.now().plusHours(1)));
        member = memberRepository.save(MemberFixture.createMemberWithEmailAndRole(INVITED_EMAIL, ROLE_학생));

        otherTransaction = new TransactionTemplate(transactionTemplate.getTransactionManager());
        otherTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @AfterEach
    void 테스트에서_커밋된_초대와_회원을_정리한다() {
        jdbcTemplate.update("DELETE FROM contest_member WHERE contest_id = ?", contest.getId());
        jdbcTemplate.update("DELETE FROM invitation_token WHERE invitation_id = ?", invitation.getId());
        jdbcTemplate.update("DELETE FROM invitation WHERE id = ?", invitation.getId());
        jdbcTemplate.update("DELETE FROM contest WHERE id = ?", contest.getId());
        jdbcTemplate.update("DELETE FROM contest_category WHERE id = ?", contestCategory.getId());
        jdbcTemplate.update("DELETE FROM member_roles WHERE member_id = ?", member.getId());
        jdbcTemplate.update("DELETE FROM member WHERE id = ?", member.getId());
    }

    @Test
    @DisplayName("[실패] 수락 요청의 스냅샷 이후 직원 역할이 부여된 회원은 초대를 수락할 수 없다.")
    void 수락_요청의_스냅샷_이후_직원_역할이_부여된_회원은_초대를_수락할_수_없다() {
        transactionTemplate.executeWithoutResult(status -> {
            // 요청 전에 회원을 미리 읽어 둔 뒤, 다른 커넥션에서 직원 역할 부여를 커밋한다.
            memberRepository.findById(member.getId());
            otherTransaction.executeWithoutResult(otherStatus -> jdbcTemplate.update(
                    "INSERT INTO member_roles (member_id, role) VALUES (?, ?)", member.getId(), ROLE_직원.name()));

            assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                    .isInstanceOf(InvitationException.class)
                    .hasMessage(CANNOT_ACCEPT_INVITATION.errorMessage());
            status.setRollbackOnly();
        });
    }

    @Test
    @DisplayName("[실패] 수락 요청의 스냅샷 이후 재발급된 이전 링크로는 초대를 수락할 수 없다.")
    void 수락_요청의_스냅샷_이후_재발급된_이전_링크로는_초대를_수락할_수_없다() {
        transactionTemplate.executeWithoutResult(status -> {
            // 요청자 트랜잭션의 스냅샷을 잡은 뒤, 다른 커넥션에서 링크 재발급을 커밋한다.
            invitationTokenRepository.count();
            otherTransaction.executeWithoutResult(otherStatus -> invitationTokenRepository.save(
                    InvitationFixture.createInvitationToken(invitation, invitationTokenProvider.hash("new-token"),
                            LocalDateTime.now().plusHours(1))));

            assertThatThrownBy(() -> invitationCommandService.acceptInvitation(member.getId(), TOKEN))
                    .isInstanceOf(InvitationException.class)
                    .hasMessage(REPLACED_INVITATION.errorMessage());
            status.setRollbackOnly();
        });
    }
}
