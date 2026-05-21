package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.CategoryDTO;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "类别模块", description = "账单类别相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取类别列表", description = "获取当前用户的账单类别列表")
    public Result<List<CategoryVO>> getCategoryList(
            @Parameter(description = "类别类型：1-支出，2-收入") @RequestParam(required = false) Integer type) {
        Long userId = UserContext.getUserId();
        List<CategoryVO> list = categoryService.getCategoryList(userId, type);
        return Result.success(list);
    }

    @PostMapping
    @OperLog("创建类别")
    @Operation(summary = "创建类别", description = "创建一个新的账单类别")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Long userId = UserContext.getUserId();
        CategoryVO vo = categoryService.createCategory(userId, categoryDTO);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @OperLog("更新类别")
    @Operation(summary = "更新类别", description = "更新指定的账单类别")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "类别ID") @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        Long userId = UserContext.getUserId();
        CategoryVO vo = categoryService.updateCategory(userId, categoryId, categoryDTO);
        return Result.success("更新成功", vo);
    }

    @DeleteMapping("/{id}")
    @OperLog("删除类别")
    @Operation(summary = "删除类别", description = "删除指定的账单类别")
    public Result<Void> deleteCategory(
            @Parameter(description = "类别ID") @PathVariable("id") Long categoryId) {
        Long userId = UserContext.getUserId();
        categoryService.deleteCategory(userId, categoryId);
        return Result.success("删除成功", null);
    }
}
