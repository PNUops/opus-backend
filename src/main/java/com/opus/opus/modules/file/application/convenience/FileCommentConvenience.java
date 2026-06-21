package com.opus.opus.modules.file.application.convenience;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.opus.opus.modules.file.application.dto.CommentFileInfo;
import com.opus.opus.modules.file.domain.FileComment;
import com.opus.opus.modules.file.domain.dao.FileCommentRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileCommentConvenience {

    private final FileCommentRepository fileCommentRepository;

    public Map<Long, List<CommentFileInfo>> findFilesGroupedByCommentIds(final List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }

        return fileCommentRepository.findAllWithFileByCommentIdIn(commentIds).stream()
                .collect(groupingBy(FileComment::getCommentId,
                        mapping(fileComment -> new CommentFileInfo(
                                fileComment.getId(),
                                fileComment.getFileName(),
                                fileComment.getFileSize()
                        ), toList())));
    }
}
