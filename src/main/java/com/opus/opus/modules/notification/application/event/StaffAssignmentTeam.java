package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record StaffAssignmentTeam(
        Long teamId,
        String teamDisplayName,
        List<Long> memberIds
) {
}
