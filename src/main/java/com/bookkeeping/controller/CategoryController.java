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

/**
 * 类别控制器
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "类别管理", description = "账单类别相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取类别列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取类别列表", description = "获取当前用户的所有类别（包括系统默认类别）")
    public Result<List<CategoryVO>> getCategoryList(
            @Parameter(description = "类型：1-收入，2-支出，不传则查询全部")
            @RequestParam(required = false) Integer type) {
        Long userId = UserContext.getUserId();
        List<CategoryVO> list = categoryService.getCategoryList(userId, type);
        return Result.success(list);
    }

    /**
     * 创建类别
     */
    @PostMapping
    @Operation(summary = "创建类别", description = "创建自定义账单类别")
    @OperLog("创建类别")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Long userId = UserContext.getUserId();
        CategoryVO vo = categoryService.createCategory(userId, categoryDTO);
        return Result.success("创建成功", vo);
    }

    /**
     * 更新类别
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新类别", description = "更新自定义账单类别")
    @OperLog("更新类别")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "类别ID", required = true)
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        Long userId = UserContext.getUserId();
        CategoryVO vo = categoryService.updateCategory(userId, categoryId, categoryDTO);
        return Result.success("更新成功", vo);
    }

    /**
     * 删除类别
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除类别", description = "删除自定义账单类别")
    @OperLog("删除类别")
    public Result<Void> deleteCategory(
            @Parameter(description = "类别ID", required = true)
            @PathVariable("id") Long categoryId) {
        Long userId = UserContext.getUserId();
        categoryService.deleteCategory(userId, categoryId);
        return Result.success("删除成功", null);
    }
}
