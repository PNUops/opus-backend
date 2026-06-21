package com.opus.opus.contest.application;

import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.file.FileFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestTrackCommandService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ContestTrackCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestTrackCommandService contestTrackCommandService;

    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private FileImageRepository fileImageRepository;

    private Contest contest;
    private ContestTrack track;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
    }

    @Test
    @DisplayName("[성공] 분과 기본 썸네일을 저장하면 기존 파일은 삭제되고 새 파일이 저장된다.")
    void 분과_기본_썸네일_저장_성공() {
        // given
        fileImageRepository.save(FileFixture.createTrackThumbnailFileImage(track.getId()));

        MultipartFile image = new MockMultipartFile("image", "new.jpg", "image/jpeg", "content".getBytes());

        // when
        contestTrackCommandService.saveContestTrackDefaultThumbnail(contest.getId(), track.getId(), image);

        // then
        verify(fileImageCommandService, times(1)).replaceImageFile(image, track.getId(), TRACK, THUMBNAIL);
    }

    @Test
    @DisplayName("[성공] 분과 기본 썸네일을 삭제한다.")
    void 분과_기본_썸네일_삭제_성공() {
        // when
        contestTrackCommandService.deleteContestTrackDefaultThumbnail(contest.getId(), track.getId());

        // then
        verify(fileImageCommandService, times(1)).deleteIfExists(track.getId(), TRACK, THUMBNAIL);
    }
}
