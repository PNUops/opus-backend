package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findAllBySubmissionIdOrderByFileOrder(Long submissionId);

    boolean existsByIdAndSubmissionId(Long id, Long submissionId);

    long countBySubmissionId(Long submissionId);

    @Query("""
             SELECT COALESCE(MAX(fd.fileOrder), 0)
             FROM FileDocument fd
             WHERE fd.submissionId = :submissionId
            """)
    int findMaxFileOrderBySubmissionId(@Param("submissionId") Long submissionId);

}
