package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findAllBySubmissionIdOrderByFileOrder(Long submissionId);

    // File 을 함께 조회해 아카이브 집계/스트리밍 시 File 지연로딩 N+1 을 방지한다.
    @Query("SELECT fd FROM FileDocument fd JOIN FETCH fd.file WHERE fd.submissionId IN :submissionIds")
    List<FileDocument> findAllBySubmissionIdIn(@Param("submissionIds") List<Long> submissionIds);

}
