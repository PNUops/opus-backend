package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberConvenience memberConvenience;
    private final FileConvenience fileConvenience;
    private final FileStorageUtil fileStorageUtil;

    public EmailFindResponse getMyEmail(final String studentId) {
        final Member member = memberConvenience.getValidateExistMemberByStudentId(studentId);
        return new EmailFindResponse(member.getEmail());
    }

    public ImageResponse getProfileImage(final Member member) {
        final File profileFile = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(member.getId(), MEMBER, PROFILE);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(profileFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public AccountInfoResponse getAccountInfo(final Long memberId) {
        final Member member = memberConvenience.getValidateExistMember(memberId);
        return AccountInfoResponse.from(member);
    }
}
