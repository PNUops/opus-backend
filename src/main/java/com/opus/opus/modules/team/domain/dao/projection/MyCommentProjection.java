package com.opus.opus.modules.team.domain.dao.projection;

import java.time.LocalDateTime;

public interface MyCommentProjection {
    Long getCommentId();
    String getContent();
    LocalDateTime getCreatedAt();
    String getMemberName();
    Long getContestId();
    String getContestName();
    String getCategoryName();
    String getTrackName();
    Long getTeamId();
    String getTeamName();
    String getProjectName();
    String getOverview();
}
