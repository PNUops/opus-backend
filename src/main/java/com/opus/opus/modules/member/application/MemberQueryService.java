package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_ORDER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_SORT_VALUE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.request.SearchConditionRequest;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.domain.dao.MyVoteResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.dao.MyProjectFlatResult;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.util.LinkedHashMap;
import java.util.List;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private static final int LIKE_PREVIEW_SIZE = 3;

    private final MemberConvenience memberConvenience;
    private final FileConvenience fileConvenience;
    private final FileStorageUtil fileStorageUtil;
    private final TeamCommentRepository teamCommentRepository;
    private final TeamLikeRepository teamLikeRepository;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamVoteRepository teamVoteRepository;

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

    public Page<MyCommentResponse> getMyComments(final Long memberId, final String sort,
                                                 final LocalDate startDate, final LocalDate endDate,
                                                 final int page, final int size) {
        validateDateRange(startDate, endDate);
        final SearchConditionRequest condition = createSearchCondition(sort, startDate, endDate, page, size);
        return teamCommentRepository.findMyComments(memberId, condition.startDateTime(), condition.endDateTime(), condition.pageable())
                .map(MyCommentResponse::from);
    }

    public List<MyLikePreviewResponse> getMyLikePreview(final Long memberId) {
        final Pageable pageable = PageRequest.of(0, LIKE_PREVIEW_SIZE);
        return teamLikeRepository.findMyRecentLikedProjects(memberId, pageable).stream()
                .map(MyLikePreviewResponse::from)
                .toList();
    }

    public Page<MyLikedProjectResponse> getMyLikedProjects(final Long memberId, final String sort,
                                                           final LocalDate startDate, final LocalDate endDate,
                                                           final Long categoryId, final Long contestId,
                                                           final int page, final int size) {
        validateDateRange(startDate, endDate);
        final SearchConditionRequest searchCondition = createSearchCondition(sort, startDate, endDate, page, size);
        return teamLikeRepository.findMyLikedProjects(memberId, searchCondition.startDateTime(), searchCondition.endDateTime(), categoryId, contestId, searchCondition.pageable())
                .map(MyLikedProjectResponse::from);
    }

    private SearchConditionRequest createSearchCondition(final String sort, final LocalDate startDate, final LocalDate endDate, final int page, final int size) {
        final Sort.Direction direction = parseSortDirection(sort);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        final LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        final LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        return new SearchConditionRequest(pageable, startDateTime, endDateTime);
    }

    private Sort.Direction parseSortDirection(final String sort) {
        if (sort == null || "latest".equalsIgnoreCase(sort)) {
            return DESC;
        }
        if ("oldest".equalsIgnoreCase(sort)) {
            return ASC;
        }
        throw new MemberException(INVALID_SORT_VALUE);
    }

    private void validateDateRange(final LocalDate startDate, final LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new MemberException(INVALID_DATE_RANGE);
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new MemberException(INVALID_DATE_ORDER);
        }
    }

}
