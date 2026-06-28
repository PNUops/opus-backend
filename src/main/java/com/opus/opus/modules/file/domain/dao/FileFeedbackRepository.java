package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileFeedbackRepository extends JpaRepository<FileFeedback, Long> {

    @Query("""
                SELECT ff FROM FileFeedback ff
                JOIN FETCH ff.file
                WHERE ff.feedbackId IN :feedbackIds
                ORDER BY ff.feedbackId ASC, ff.fileOrder ASC
            """)
    List<FileFeedback> findAllWithFileByFeedbackIdIn(List<Long> feedbackIds);

    List<FileFeedback> findAllByFeedbackIdOrderByFileOrder(Long feedbackId);

    @Query("SELECT COALESCE(MAX(ff.fileOrder), -1) FROM FileFeedback ff WHERE ff.feedbackId = :feedbackId")
    int findMaxFileOrderByFeedbackId(Long feedbackId);

    long countByFeedbackId(Long feedbackId);

    Optional<FileFeedback> findByIdAndFeedbackId(Long id, Long feedbackId);
}
