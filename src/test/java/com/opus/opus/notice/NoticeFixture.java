package com.opus.opus.notice;

import com.opus.opus.modules.notice.domain.Notice;

public class NoticeFixture {

    public static Notice createGlobalNotice() {
        return Notice.builder()
                .title("전체 공지 제목입니다.")
                .contestId(null)
                .description("전체 공지 내용입니다.")
                .build();
    }

    public static Notice createContestNotice() {
        return Notice.builder()
                .title("대회 공지 제목입니다.")
                .contestId(1L)
                .description("대회 공지 내용입니다.")
                .build();
    }
}
