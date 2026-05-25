package com.opus.opus.modules.notification.application;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.dto.response.NotificationResponse;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    private final MemberConvenience memberConvenience;

    public List<NotificationResponse> getNotifications(final Member member) {
        return notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
