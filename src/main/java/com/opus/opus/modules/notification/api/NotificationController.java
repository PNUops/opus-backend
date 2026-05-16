package com.opus.opus.modules.notification.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.NotificationCommandService;
import com.opus.opus.modules.notification.application.NotificationQueryService;
import com.opus.opus.modules.notification.application.dto.response.NotificationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@LoginMember final Member member) {
        return ResponseEntity.ok(notificationQueryService.getNotifications(member));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<Void> updateSingleNotification(@PathVariable final Long notificationId,
                                                         @LoginMember final Member member) {
        notificationCommandService.updateSingleNotification(notificationId, member);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<Void> updateNotificationAll(@LoginMember final Member member) {
        notificationCommandService.updateNotificationAll(member);
        return ResponseEntity.noContent().build();
    }
}
