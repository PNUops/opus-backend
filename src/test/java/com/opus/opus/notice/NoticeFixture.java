package com.opus.opus.notice;

import com.opus.opus.modules.notice.domain.Notice;

public class NoticeFixture {

    public static Notice createNotice() {
        return Notice.builder()
                .title("공지 제목입니다.")
                .description("공지 내용입니다.")
                .build();
    }
}
