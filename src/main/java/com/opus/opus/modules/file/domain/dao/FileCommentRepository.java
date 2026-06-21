package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileCommentRepository extends JpaRepository<FileComment, Long> {

    @Query("""
                SELECT fc FROM FileComment fc
                JOIN FETCH fc.file
                WHERE fc.commentId IN :commentIds
                ORDER BY fc.commentId ASC, fc.fileOrder ASC
            """)
    List<FileComment> findAllWithFileByCommentIdIn(List<Long> commentIds);

    List<FileComment> findAllByCommentIdOrderByFileOrder(Long commentId);
}
