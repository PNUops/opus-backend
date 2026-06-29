package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findAllBySubmissionIdOrderByFileOrder(Long submissionId);

    // 여러 제출물의 파일 정보를 (제출ID·파일문서ID·파일명·크기)으로 한 번에 조회 (zip 구성·멘토 조회용)
    @Query("""
            SELECT new com.opus.opus.modules.file.domain.dao.SubmissionFileInfo(
                   fd.submissionId, fd.id, f.name, f.fileSize)
            FROM FileDocument fd
            JOIN fd.file f
            WHERE fd.submissionId IN :submissionIds
            ORDER BY fd.submissionId, fd.fileOrder
            """)
    List<SubmissionFileInfo> findFilesBySubmissionIds(@Param("submissionIds") List<Long> submissionIds);

    List<FileDocument> findAllBySubmissionIdInOrderBySubmissionIdAscFileOrderAsc(List<Long> submissionIds);

    boolean existsByIdAndSubmissionId(Long id, Long submissionId);

    long countBySubmissionId(Long submissionId);

    @Query("""
             SELECT COALESCE(MAX(fd.fileOrder), 0)
             FROM FileDocument fd
             WHERE fd.submissionId = :submissionId
            """)
    int findMaxFileOrderBySubmissionId(@Param("submissionId") Long submissionId);

}
