package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamCommentRepository extends JpaRepository<TeamComment, Long> {
    List<TeamComment> findAllByTeamIdOrderByIdDesc(Long id);

    void deleteAllByTeamId(Long teamId);
}
