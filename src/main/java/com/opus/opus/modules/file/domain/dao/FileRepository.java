package com.opus.opus.modules.file.domain.dao;

import com.opus.opus.modules.file.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
