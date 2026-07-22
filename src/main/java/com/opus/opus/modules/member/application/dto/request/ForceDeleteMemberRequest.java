package com.opus.opus.modules.member.application.dto.request;

import com.opus.opus.modules.member.domain.MemberWithdrawalReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ForceDeleteMemberRequest(

        @NotNull(message = "탈퇴 사유를 선택해주세요.")
        MemberWithdrawalReason reason,

        @Size(max = 500, message = "기타 사항은 500자 이하로 입력해주세요.")
        String detail
) {
}
