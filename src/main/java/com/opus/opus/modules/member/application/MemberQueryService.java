package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_ORDER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.request.SearchConditionRequest;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    private final TeamCommentRepository teamCommentRepository;
    private final TeamLikeRepository teamLikeRepository;

    public EmailFindResponse getMyEmail(final String studentId) {
        final Member member = memberConvenience.getValidateExistMemberByStudentId(studentId);
        return new EmailFindResponse(member.getEmail());
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
        final Sort.Direction direction = "oldest".equalsIgnoreCase(sort) ? ASC : DESC;
        final Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        final LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        final LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        return new SearchConditionRequest(pageable, startDateTime, endDateTime);
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
