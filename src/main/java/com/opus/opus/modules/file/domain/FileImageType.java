package com.opus.opus.modules.file.domain;

public enum FileImageType {

    PREVIEW,
    THUMBNAIL,
    BANNER,
    POSTER,
    PROFILE,
    ;

    public boolean isSinglePerReference() {
        return this != PREVIEW;
    }
}

