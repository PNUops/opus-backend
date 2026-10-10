package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record StaffUnassignedNotificationEvent(
        Long contestId,
        Long staffId,
        String staffName,
        StaffPosition position,
        List<StaffAssignmentTeam> teams
) {
}
