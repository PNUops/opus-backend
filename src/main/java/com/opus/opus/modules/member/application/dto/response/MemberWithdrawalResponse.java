package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberWithdrawalHistory;
import com.opus.opus.modules.member.domain.MemberWithdrawalReason;
import java.time.LocalDateTime;

public record MemberWithdrawalResponse(

        Long memberId,

        String name,

        String studentId,

        String email,

        MemberWithdrawalReason reason,

        String detail,

        LocalDateTime withdrawnAt,

        ProcessedAdmin processedBy
) {

    public record ProcessedAdmin(

            Long memberId,

            String name
    ) {
    }

    // 탈퇴 이력이 없는 회원(이력 저장 도입 이전 탈퇴)은 사유·시각이 null, 본인 탈퇴는 processedBy가 null이다.
    public static MemberWithdrawalResponse of(final Member member, final MemberWithdrawalHistory history,
                                              final Member admin) {
        return new MemberWithdrawalResponse(
                member.getId(),
                member.getName(),
                member.getStudentId(),
                member.getEmail(),
                history == null ? null : history.getReason(),
                history == null ? null : history.getDetail(),
                history == null ? null : history.getCreatedAt(),
                admin == null ? null : new ProcessedAdmin(admin.getId(), admin.getName())
        );
    }
}
