package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;

import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.domain.dao.MyVoteResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.dao.MyProjectFlatResult;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.util.LinkedHashMap;
import java.util.List;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import com.opus.opus.modules.file.application.dto.FileResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberConvenience memberConvenience;
    private final FileConvenience fileConvenience;
    private final FileQueryService fileQueryService;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamVoteRepository teamVoteRepository;

    public EmailFindResponse getMyEmail(final String studentId) {
        final Member member = memberConvenience.getValidateExistMemberByStudentId(studentId);
        return new EmailFindResponse(member.getEmail());
    }

    public ImageResponse getProfileImage(final Member member) {
        final File profileFile = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(member.getId(), MEMBER, PROFILE);
        final FileResource storageResult = fileQueryService.findFileAndType(profileFile.getId());
        return new ImageResponse(storageResult.resource(), storageResult.mimeType());
    }

    public AccountInfoResponse getAccountInfo(final Long memberId) {
        final Member member = memberConvenience.getValidateExistMember(memberId);
        return AccountInfoResponse.from(member);
    }

    public List<MyProjectResponse> getMyProjects(final Long memberId) {
        return teamMemberRepository.findMyProjectsWithAwards(memberId).stream()
                .collect(groupingBy(MyProjectFlatResult::teamId, LinkedHashMap::new, toList()))
                .values().stream()
                .map(MyProjectResponse::from)
                .toList();
    }

    public List<MyVoteResponse> getMyVotes(final Long memberId) {
        return teamVoteRepository.findMyVotes(memberId);
    }
}
