package com.opus.opus.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MemberQueryServiceTest extends IntegrationTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 가입된 회원은 이메일 찾기를 할 수 있다.")
    void 가입된_회원은_이메일_찾기를_할_수_있다() {
        final EmailFindResponse response = memberQueryService.getMyEmail(member.getStudentId());

        assertThat(response.email()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("[실패] 미가입 회원은 이메일 찾기가 불가하다.")
    void 미가입_회원은_이메일_찾기가_불가하다() {
        final String notExistMemberEmail = "qwqw@pusan.ac.kr";

        assertThatThrownBy(() -> {
            memberQueryService.getMyEmail(notExistMemberEmail);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }
}
