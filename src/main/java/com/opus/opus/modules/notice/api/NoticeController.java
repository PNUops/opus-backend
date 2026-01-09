package com.opus.opus.modules.notice.api;

import com.opus.opus.modules.notice.application.NoticeCommandService;
import com.opus.opus.modules.notice.application.NoticeQueryService;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
import com.opus.opus.modules.notice.application.dto.response.NoticeDetailResponse;
import com.opus.opus.modules.notice.application.dto.response.NoticeSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    @Secured("ROLE_관리자")
    @PostMapping("/notices")
    public ResponseEntity<Void> createNotice(@Valid @RequestBody final NoticeRequest request) {
        noticeCommandService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/notices/{noticeId}")
    public ResponseEntity<Void> updateNotice(@Valid @RequestBody final NoticeRequest request,
                                             @PathVariable final Long noticeId) {
        noticeCommandService.updateNotice(request, noticeId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable final Long noticeId) {
        noticeCommandService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable final Long noticeId) {
        return ResponseEntity.ok(noticeQueryService.getNotice(noticeId));
    }

    @GetMapping("/notices")
    public ResponseEntity<List<NoticeSummaryResponse>> getAllNotices() {
        return ResponseEntity.ok(noticeQueryService.getAllNotices());
    }

    @Secured("ROLE_관리자")
    @PostMapping("/contests/{contestId}/notices")
    public ResponseEntity<Void> createContestNotice(@PathVariable final Long contestId,
                                                    @Valid @RequestBody final NoticeRequest request) {
        noticeCommandService.createContestNotice(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/contests/{contestId}/notices/{noticeId}")
    public ResponseEntity<Void> updateContestNotice(@Valid @RequestBody final NoticeRequest request,
                                                    @PathVariable final Long contestId,
                                                    @PathVariable final Long noticeId) {
        noticeCommandService.updateContestNotice(request, contestId, noticeId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/contests/{contestId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteContestNotice(@PathVariable final Long contestId,
                                                    @PathVariable final Long noticeId) {
        noticeCommandService.deleteContestNotice(contestId, noticeId);
        return ResponseEntity.noContent().build();
    }
}
