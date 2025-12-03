package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByReferenceIdAndReferenceTypeAndImageType(Long referenceId, ReferenceDomainType referenceType,
                                                                 FileImageType imageType);

    long countByReferenceIdAndReferenceTypeAndImageType(Long referenceId, ReferenceDomainType referenceType,
                                                        FileImageType imageType);
}
