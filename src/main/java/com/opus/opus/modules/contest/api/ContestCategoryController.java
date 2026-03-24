package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestCategoryCommandService;
import com.opus.opus.modules.contest.application.ContestCategoryQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class ContestCategoryController {

    private final ContestCategoryCommandService contestCategoryCommandService;
    private final ContestCategoryQueryService contestCategoryQueryService;

    @Secured("ROLE_관리자")
    @PostMapping
    public ResponseEntity<Void> createContestCategory(@Valid @RequestBody final ContestCategoryRequest request) {
        contestCategoryCommandService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<Void> updateContestCategory(@Valid @RequestBody final ContestCategoryRequest request,
                                                      @PathVariable final Long categoryId) {
        contestCategoryCommandService.updateCategory(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteContestCategory(@PathVariable final Long categoryId) {
        contestCategoryCommandService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContestCategoryResponse>> getAllContestCategories() {
        List<ContestCategoryResponse> response = contestCategoryQueryService.getAllContestCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sidebar")
    public ResponseEntity<List<SidebarResponse>> getSidebar() {
        final List<SidebarResponse> response = contestCategoryQueryService.getSidebar();
        return ResponseEntity.ok(response);
    }
}
