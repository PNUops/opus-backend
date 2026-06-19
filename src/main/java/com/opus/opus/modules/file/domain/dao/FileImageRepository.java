package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileImageRepository extends JpaRepository<FileImage, Long> {

    Optional<FileImage> findByReferenceIdAndReferenceTypeAndImageType(
            Long referenceId, ReferenceDomainType referenceType, FileImageType imageType);

    List<FileImage> findAllByReferenceIdAndReferenceTypeAndImageType(
            Long referenceId, ReferenceDomainType referenceType, FileImageType imageType);

    long countByReferenceIdAndReferenceTypeAndImageType(
            Long referenceId, ReferenceDomainType referenceType, FileImageType imageType);

    List<FileImage> findAllByReferenceIdAndReferenceType(Long referenceId, ReferenceDomainType referenceType);

}
