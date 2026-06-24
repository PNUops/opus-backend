package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findAllBySubmissionIdOrderByFileOrder(Long submissionId);

    List<FileDocument> findAllBySubmissionIdIn(List<Long> submissionIds);

}
