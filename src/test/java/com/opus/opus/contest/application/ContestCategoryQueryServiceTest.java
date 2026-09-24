package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.domain.SidebarSortType.CUSTOM;
import static com.opus.opus.modules.contest.domain.SidebarSortType.DESC;
import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestCategoryCommandService;
import com.opus.opus.modules.contest.application.ContestCategoryQueryService;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortRequest;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestCategoryQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestCategoryQueryService contestCategoryQueryService;

    @Autowired
    private ContestCategoryCommandService contestCategoryCommandService;

    @Autowired
    private ContestCategoryRepository contestCategoryRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Test
    @DisplayName("[성공] 카테고리가 없으면 빈 리스트를 반환한다.")
    void 카테고리가_없으면_빈_리스트를_반환한다() {
        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("[성공] 대회가 없는 카테고리는 contests가 빈 리스트로 반환된다.")
    void 대회가_없는_카테고리는_contests가_빈_리스트로_반환된다() {
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).contests()).isEmpty();
    }

    @Test
    @DisplayName("[성공] 카테고리에 속한 대회들이 올바르게 반환된다.")
    void 카테고리에_속한_대회들이_올바르게_반환된다() {
        final ContestCategory category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contestRepository.save(ContestFixture.createContestWithCategoryId(category.getId()));
        contestRepository.save(ContestFixture.createContestWithCategoryId(category.getId()));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).categoryId()).isEqualTo(category.getId());
        assertThat(response.get(0).categoryName()).isEqualTo(category.getCategoryName());
        assertThat(response.get(0).contests()).hasSize(2);
    }

    @Test
    @DisplayName("[성공] 대회들이 각자의 카테고리에 맞게 분류된다.")
    void 대회들이_각자의_카테고리에_맞게_분류된다() {
        final ContestCategory categoryA = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        final ContestCategory categoryB = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contestRepository.save(ContestFixture.createContestWithCategoryId(categoryA.getId()));
        contestRepository.save(ContestFixture.createContestWithCategoryId(categoryA.getId()));
        contestRepository.save(ContestFixture.createContestWithCategoryId(categoryB.getId()));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        final SidebarResponse responseA = response.stream()
                .filter(r -> r.categoryId().equals(categoryA.getId()))
                .findFirst().orElseThrow();
        final SidebarResponse responseB = response.stream()
                .filter(r -> r.categoryId().equals(categoryB.getId()))
                .findFirst().orElseThrow();

        assertThat(responseA.contests()).hasSize(2);
        assertThat(responseB.contests()).hasSize(1);
    }

    @Test
    @DisplayName("[성공] 카테고리 정렬 기본값(ASC)이면 카테고리명 오름차순으로 정렬된다.")
    void 카테고리_기본값_ASC면_이름_오름차순으로_정렬된다() {
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("다카테고리"));
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("가카테고리"));
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("나카테고리"));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).extracting(SidebarResponse::categoryName)
                .containsExactly("가카테고리", "나카테고리", "다카테고리");
    }

    @Test
    @DisplayName("[성공] 카테고리 정렬이 DESC면 카테고리명 내림차순으로 정렬된다.")
    void 카테고리_정렬이_DESC면_이름_내림차순으로_정렬된다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(DESC));
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("다카테고리"));
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("가카테고리"));
        contestCategoryRepository.save(ContestCategoryFixture.createContestCategoryWithName("나카테고리"));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).extracting(SidebarResponse::categoryName)
                .containsExactly("다카테고리", "나카테고리", "가카테고리");
    }

    @Test
    @DisplayName("[성공] 카테고리 정렬이 CUSTOM이면 itemOrder 순으로 정렬된다.")
    void 카테고리_정렬이_CUSTOM이면_itemOrder_순으로_정렬된다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryLast = contestCategoryRepository.save(
                ContestCategoryFixture.createContestCategoryWithName("가카테고리"));
        final ContestCategory categoryFirst = contestCategoryRepository.save(
                ContestCategoryFixture.createContestCategoryWithName("나카테고리"));
        categoryLast.updateItemOrder(2);
        categoryFirst.updateItemOrder(1);

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response).extracting(SidebarResponse::categoryName)
                .containsExactly("나카테고리", "가카테고리");
    }

    @Test
    @DisplayName("[성공] 카테고리 내 대회 정렬 기본값(ASC)이면 대회명 오름차순으로 정렬된다.")
    void 카테고리_내_대회_기본값_ASC면_이름_오름차순으로_정렬된다() {
        final ContestCategory category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "다대회"));
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "가대회"));
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "나대회"));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response.get(0).contests()).extracting(SidebarResponse.ContestItem::contestName)
                .containsExactly("가대회", "나대회", "다대회");
    }

    @Test
    @DisplayName("[성공] 카테고리 내 대회 정렬이 DESC면 대회명 내림차순으로 정렬된다.")
    void 카테고리_내_대회_정렬이_DESC면_이름_내림차순으로_정렬된다() {
        final ContestCategory category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(), new CategoryContestSortRequest(DESC));
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "다대회"));
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "가대회"));
        contestRepository.save(ContestFixture.createContestWithCategoryIdAndName(category.getId(), "나대회"));

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response.get(0).contests()).extracting(SidebarResponse.ContestItem::contestName)
                .containsExactly("다대회", "나대회", "가대회");
    }

    @Test
    @DisplayName("[성공] 카테고리 내 대회 정렬이 CUSTOM이면 itemOrder 순으로 정렬된다.")
    void 카테고리_내_대회_정렬이_CUSTOM이면_itemOrder_순으로_정렬된다() {
        final ContestCategory category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(), new CategoryContestSortRequest(CUSTOM));
        final Contest contestLast = contestRepository.save(
                ContestFixture.createContestWithCategoryIdAndName(category.getId(), "가대회"));
        final Contest contestFirst = contestRepository.save(
                ContestFixture.createContestWithCategoryIdAndName(category.getId(), "나대회"));
        contestLast.updateItemOrder(2);
        contestFirst.updateItemOrder(1);

        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();

        assertThat(response.get(0).contests()).extracting(SidebarResponse.ContestItem::contestName)
                .containsExactly("나대회", "가대회");
    }
}
