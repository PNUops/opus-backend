package com.opus.opus.modules.notification.application.event;

import static com.opus.opus.modules.notification.domain.NotificationType.ADVISOR_ASSIGNED;
import static com.opus.opus.modules.notification.domain.NotificationType.ADVISOR_UNASSIGNED;
import static com.opus.opus.modules.notification.domain.NotificationType.MENTOR_ASSIGNED;
import static com.opus.opus.modules.notification.domain.NotificationType.MENTOR_UNASSIGNED;

import com.opus.opus.modules.notification.domain.NotificationType;
import lombok.Getter;

@Getter
public enum StaffPosition {
    ADVISOR("지도교수", "교수님", ADVISOR_ASSIGNED, ADVISOR_UNASSIGNED),
    MENTOR("멘토", "멘토님", MENTOR_ASSIGNED, MENTOR_UNASSIGNED),
    ;

    private final String positionName;
    private final String honorific;
    private final NotificationType assignedType;
    private final NotificationType unassignedType;

    StaffPosition(final String positionName, final String honorific,
                  final NotificationType assignedType, final NotificationType unassignedType) {
        this.positionName = positionName;
        this.honorific = honorific;
        this.assignedType = assignedType;
        this.unassignedType = unassignedType;
    }
}
