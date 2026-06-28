package com.opus.opus.modules.contest.application.dto.request;

import java.util.List;

public record StaffTeamUpdateRequest(

        List<Long> addTeamIds,

        List<Long> deleteTeamIds
) {

    public StaffTeamUpdateRequest {
        addTeamIds = addTeamIds != null ? addTeamIds : List.of();
        deleteTeamIds = deleteTeamIds != null ? deleteTeamIds : List.of();
    }
}
