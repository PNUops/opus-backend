package com.opus.opus.modules.member.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opus.opus.modules.member.domain.MemberWithdrawalReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ForceDeleteMemberRequest(

        @NotNull(message = "탈퇴 사유를 선택해주세요.")
        MemberWithdrawalReason reason,

        @Size(max = 500, message = "기타 사항은 500자 이하로 입력해주세요.")
        String detail
) {

    @JsonIgnore
    @AssertTrue(message = "기타 사유를 선택한 경우 상세 사유를 입력해주세요.")
    public boolean isDetailPresentWhenEtc() {
        return reason != MemberWithdrawalReason.ETC || (detail != null && !detail.isBlank());
    }
}
