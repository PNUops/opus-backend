package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestCategoryFixture.createContestCategory;
import static com.opus.opus.contest.ContestFixture.createContestWithCategoryId;
import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;
import static com.opus.opus.modules.contest.domain.SidebarSortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_CATEGORY_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_CONTEST_ID_IN_CATEGORY_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_ITEM_ORDER_IN_CATEGORY_CONTEST_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_ITEM_ORDER_IN_CATEGORY_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_CONTEST_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_CONTEST_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.NOT_EXIST_CONTEST_IN_CATEGORY;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestCategoryCommandService;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortRequest;
import com.opus.opus.modules.contest.domain.CategoryContestSort;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.CategoryContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.SidebarCategorySortRepository;
import com.opus.opus.modules.contest.exception.ContestCategoryException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestCategoryCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestCategoryCommandService contestCategoryCommandService;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private SidebarCategorySortRepository sidebarCategorySortRepository;
    @Autowired
    private CategoryContestSortRepository categoryContestSortRepository;

    @Test
    @DisplayName("[성공] 카테고리를 생성하면 itemOrder와 기본 카테고리 내 대회 정렬(ASC)이 함께 생성된다.")
    void 카테고리를_생성하면_itemOrder와_기본_대회_정렬이_함께_생성된다() {
        contestCategoryCommandService.createCategory(new ContestCategoryRequest("첫번째 카테고리"));

        final ContestCategory category = contestCategoryRepository.findAll().get(0);
        assertThat(category.getItemOrder()).isEqualTo(1);

        final CategoryContestSort sort = categoryContestSortRepository.findByCategoryId(category.getId())
                .orElseThrow();
        assertThat(sort.getMode()).isEqualTo(ASC);
    }

    @Test
    @DisplayName("[성공] 카테고리를 삭제하면 카테고리 내 대회 정렬 설정도 함께 삭제된다.")
    void 카테고리를_삭제하면_대회_정렬_설정도_함께_삭제된다() {
        contestCategoryCommandService.createCategory(new ContestCategoryRequest("삭제될 카테고리"));
        final ContestCategory category = contestCategoryRepository.findAll().get(0);

        contestCategoryCommandService.deleteCategory(category.getId());

        assertThat(categoryContestSortRepository.findByCategoryId(category.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] 카테고리 정렬 모드를 변경하면 모드가 반영된다.")
    void 카테고리_정렬_모드를_변경하면_모드가_반영된다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));

        final var sort = sidebarCategorySortRepository.findFirstByOrderByIdAsc().orElseThrow();
        assertThat(sort.getMode()).isEqualTo(CUSTOM);
    }

    @Test
    @DisplayName("[성공] 카테고리 수동 정렬을 하면 itemOrder가 반영된다.")
    void 카테고리_수동_정렬을_하면_itemOrder가_반영된다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        final ContestCategory categoryTwo = contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 2),
                new SidebarCategorySortCustomRequest(categoryTwo.getId(), 1));

        contestCategoryCommandService.updateCategorySortCustom(requests);

        assertThat(categoryOne.getItemOrder()).isEqualTo(2);
        assertThat(categoryTwo.getItemOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] CUSTOM 모드가 아니라면 카테고리 수동 정렬은 실패한다.")
    void CUSTOM모드가_아니라면_카테고리_수동_정렬은_실패한다() {
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateCategorySortCustom(requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 categoryId가 있다면 카테고리 수동 정렬은 실패한다.")
    void 중복_categoryId가_있다면_카테고리_수동_정렬은_실패한다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 1),
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 2));

        assertThatThrownBy(() -> contestCategoryCommandService.updateCategorySortCustom(requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(DUPLICATE_CATEGORY_ID_IN_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 itemOrder가 있다면 카테고리 수동 정렬은 실패한다.")
    void 중복_itemOrder가_있다면_카테고리_수동_정렬은_실패한다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        final ContestCategory categoryTwo = contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 1),
                new SidebarCategorySortCustomRequest(categoryTwo.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateCategorySortCustom(requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(DUPLICATE_ITEM_ORDER_IN_CATEGORY_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 list size가 카테고리 개수와 다르면 카테고리 수동 정렬은 실패한다.")
    void 요청받은_list_size가_카테고리_개수와_다르면_카테고리_수동_정렬은_실패한다() {
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateCategorySortCustom(requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(INVALID_CATEGORY_SORT_CUSTOM_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 itemOrder가 카테고리 개수를 넘어가면 카테고리 수동 정렬은 실패한다.")
    void 요청받은_itemOrder가_카테고리_개수를_넘어가면_카테고리_수동_정렬은_실패한다() {
        final int invalidItemOrder = 99;
        contestCategoryCommandService.updateCategorySort(new SidebarCategorySortRequest(CUSTOM));
        final ContestCategory categoryOne = contestCategoryRepository.save(createContestCategory());
        final List<SidebarCategorySortCustomRequest> requests = List.of(
                new SidebarCategorySortCustomRequest(categoryOne.getId(), invalidItemOrder));

        assertThatThrownBy(() -> contestCategoryCommandService.updateCategorySortCustom(requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(INVALID_CATEGORY_ITEM_ORDER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 카테고리 내 대회 정렬 모드를 변경하면 모드가 반영된다.")
    void 카테고리_내_대회_정렬_모드를_변경하면_모드가_반영된다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());

        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));

        final CategoryContestSort sort = categoryContestSortRepository.findByCategoryId(category.getId())
                .orElseThrow();
        assertThat(sort.getMode()).isEqualTo(CUSTOM);
    }

    @Test
    @DisplayName("[성공] 카테고리 내 대회 수동 정렬을 하면 itemOrder가 반영된다.")
    void 카테고리_내_대회_수동_정렬을_하면_itemOrder가_반영된다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        final Contest contestTwo = contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), 2),
                new CategoryContestSortCustomRequest(contestTwo.getId(), 1));

        contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests);

        assertThat(contestOne.getItemOrder()).isEqualTo(2);
        assertThat(contestTwo.getItemOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] CUSTOM 모드가 아니라면 카테고리 내 대회 수동 정렬은 실패한다.")
    void CUSTOM모드가_아니라면_카테고리_내_대회_수동_정렬은_실패한다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 contestId가 있다면 카테고리 내 대회 수동 정렬은 실패한다.")
    void 중복_contestId가_있다면_카테고리_내_대회_수동_정렬은_실패한다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), 1),
                new CategoryContestSortCustomRequest(contestOne.getId(), 2));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(DUPLICATE_CONTEST_ID_IN_CATEGORY_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 itemOrder가 있다면 카테고리 내 대회 수동 정렬은 실패한다.")
    void 중복_itemOrder가_있다면_카테고리_내_대회_수동_정렬은_실패한다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        final Contest contestTwo = contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), 1),
                new CategoryContestSortCustomRequest(contestTwo.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(DUPLICATE_ITEM_ORDER_IN_CATEGORY_CONTEST_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 list size가 대회 개수와 다르면 카테고리 내 대회 수동 정렬은 실패한다.")
    void 요청받은_list_size가_대회_개수와_다르면_카테고리_내_대회_수동_정렬은_실패한다() {
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(INVALID_CATEGORY_CONTEST_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 itemOrder가 대회 개수를 넘어가면 카테고리 내 대회 수동 정렬은 실패한다.")
    void 요청받은_itemOrder가_대회_개수를_넘어가면_카테고리_내_대회_수동_정렬은_실패한다() {
        final int invalidItemOrder = 99;
        final ContestCategory category = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(category.getId(),
                new CategoryContestSortRequest(CUSTOM));
        final Contest contestOne = contestRepository.save(createContestWithCategoryId(category.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestOne.getId(), invalidItemOrder));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(category.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(INVALID_CATEGORY_CONTEST_ITEM_ORDER.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 카테고리의 대회 ID가 섞여 있으면 카테고리 내 대회 수동 정렬은 실패한다.")
    void 다른_카테고리의_대회_ID가_섞여_있으면_카테고리_내_대회_수동_정렬은_실패한다() {
        final ContestCategory categoryA = contestCategoryRepository.save(createContestCategory());
        final ContestCategory categoryB = contestCategoryRepository.save(createContestCategory());
        contestCategoryCommandService.updateContestSortInCategory(categoryA.getId(),
                new CategoryContestSortRequest(CUSTOM));
        contestRepository.save(createContestWithCategoryId(categoryA.getId()));
        final Contest contestInOtherCategory = contestRepository.save(createContestWithCategoryId(categoryB.getId()));
        final List<CategoryContestSortCustomRequest> requests = List.of(
                new CategoryContestSortCustomRequest(contestInOtherCategory.getId(), 1));

        assertThatThrownBy(() -> contestCategoryCommandService.updateContestSortInCategoryCustom(categoryA.getId(), requests))
                .isInstanceOf(ContestCategoryException.class)
                .hasMessage(NOT_EXIST_CONTEST_IN_CATEGORY.errorMessage());
    }
}
