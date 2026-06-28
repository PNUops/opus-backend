package com.opus.opus.modules.file.application.convenience;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import com.opus.opus.modules.file.domain.FileFeedback;
import com.opus.opus.modules.file.domain.dao.FileFeedbackRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileFeedbackConvenience {

    private final FileFeedbackRepository fileFeedbackRepository;

    public Map<Long, List<FeedbackFileInfo>> findFilesGroupedByFeedbackIds(final List<Long> feedbackIds) {
        if (feedbackIds.isEmpty()) {
            return Map.of();
        }

        return fileFeedbackRepository.findAllWithFileByFeedbackIdIn(feedbackIds).stream()
                .collect(groupingBy(FileFeedback::getFeedbackId,
                        mapping(fileFeedback -> new FeedbackFileInfo(
                                fileFeedback.getId(),
                                fileFeedback.getFileName(),
                                fileFeedback.getFileSize()
                        ), toList())));
    }
}
