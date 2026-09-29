package com.opus.opus.modules.contest.application.convenience;

import com.opus.opus.modules.contest.domain.SidebarCategorySort;
import com.opus.opus.modules.contest.domain.dao.SidebarCategorySortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class SidebarCategorySortInitializer {

    private final SidebarCategorySortRepository sidebarCategorySortRepository;

    /**
     * 별도의 물리 트랜잭션에서 존재를 보장한다. 호출자의 트랜잭션과 완전히 분리해야 하는 이유:
     * 1) IDENTITY 전략상 유니크 제약 위반 INSERT는 그 트랜잭션의 영속성 컨텍스트를 사용 불가 상태로 만든다.
     * 2) 이 메서드가 커밋된 뒤에야 호출자가 자신의 트랜잭션에서 처음으로 조회해야, REPEATABLE READ 스냅샷이
     *    "행이 없음"으로 먼저 고정되어 방금 커밋된 행을 못 보는 문제를 피할 수 있다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureExists() {
        if (sidebarCategorySortRepository.count() > 0) {
            return;
        }
        try {
            sidebarCategorySortRepository.save(SidebarCategorySort.createDefault());
        } catch (final DataIntegrityViolationException e) {
            // 동시에 다른 트랜잭션이 먼저 생성했다면 그대로 둔다.
        }
    }
}
