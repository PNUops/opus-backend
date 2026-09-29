package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestCategoryCommandService;
import com.opus.opus.modules.contest.application.ContestCategoryQueryService;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortRequest;
import com.opus.opus.modules.contest.application.dto.response.CategoryContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarCategorySortResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ContestCategoryController {

    private final ContestCategoryCommandService contestCategoryCommandService;
    private final ContestCategoryQueryService contestCategoryQueryService;

    @Secured("ROLE_관리자")
    @PostMapping("/categories")
    public ResponseEntity<Void> createContestCategory(@Valid @RequestBody final ContestCategoryRequest request) {
        contestCategoryCommandService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<Void> updateContestCategory(@Valid @RequestBody final ContestCategoryRequest request,
                                                      @PathVariable final Long categoryId) {
        contestCategoryCommandService.updateCategory(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteContestCategory(@PathVariable final Long categoryId) {
        contestCategoryCommandService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ContestCategoryResponse>> getAllContestCategories() {
        List<ContestCategoryResponse> response = contestCategoryQueryService.getAllContestCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sidebar")
    public ResponseEntity<List<SidebarResponse>> getSidebar() {
        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @PutMapping("/categories/sort")
    public ResponseEntity<Void> updateCategorySort(@Valid @RequestBody final SidebarCategorySortRequest request) {
        contestCategoryCommandService.updateCategorySort(request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/categories/sort")
    public ResponseEntity<SidebarCategorySortResponse> getCategorySort() {
        return ResponseEntity.ok(contestCategoryQueryService.getCategorySort());
    }

    @Secured("ROLE_관리자")
    @PutMapping("/categories/sort/custom")
    public ResponseEntity<Void> updateCategorySortCustom(
            @Valid @RequestBody final List<SidebarCategorySortCustomRequest> requests) {
        contestCategoryCommandService.updateCategorySortCustom(requests);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @PutMapping("/categories/{categoryId}/contests/sort")
    public ResponseEntity<Void> updateContestSortInCategory(@PathVariable final Long categoryId,
                                                            @Valid @RequestBody final CategoryContestSortRequest request) {
        contestCategoryCommandService.updateContestSortInCategory(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/categories/{categoryId}/contests/sort")
    public ResponseEntity<CategoryContestSortResponse> getContestSortInCategory(@PathVariable final Long categoryId) {
        return ResponseEntity.ok(contestCategoryQueryService.getContestSortInCategory(categoryId));
    }

    @Secured("ROLE_관리자")
    @PutMapping("/categories/{categoryId}/contests/sort/custom")
    public ResponseEntity<Void> updateContestSortInCategoryCustom(
            @PathVariable final Long categoryId,
            @Valid @RequestBody final List<CategoryContestSortCustomRequest> requests) {
        contestCategoryCommandService.updateContestSortInCategoryCustom(categoryId, requests);
        return ResponseEntity.noContent().build();
    }
}
