package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record StaffAssignedNotificationEvent(
        Long contestId,
        String contestName,
        Long staffId,
        String staffName,
        StaffPosition position,
        List<StaffAssignmentTeam> teams
) {
}
