package com.opus.opus.modules.notice.api;

import com.opus.opus.modules.notice.application.NoticeCommandService;
import com.opus.opus.modules.notice.application.NoticeQueryService;
import com.opus.opus.modules.notice.application.dto.request.NoticeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    @PostMapping("/notices")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> createNotice(@Valid @RequestBody final NoticeRequest request) {
        noticeCommandService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
