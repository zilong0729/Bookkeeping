package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.CreateCategoryReqVO;
import com.bookkeeping.vo.req.IdReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.req.UpdateCategoryReqVO;
import com.bookkeeping.vo.req.CategoryListReqVO;
import com.bookkeeping.vo.resp.CategoryRespVO;
import com.bookkeeping.vo.resp.Result;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/list")
    @Operation(summary = "获取类别列表", description = "获取当前用户的账单类别列表")
    public Result<List<CategoryRespVO>> list(@Valid @RequestBody UnifiedRequest<CategoryListReqVO> req) {
        Long userId = UserContext.getUserId();
        List<CategoryRespVO> list = categoryService.getCategoryList(userId, req.getData());
        return Result.success(list);
    }

    @PostMapping("/create")
    @OperLog("创建类别")
    @Operation(summary = "创建类别", description = "创建一个新的账单类别")
    public Result<CategoryRespVO> create(@Valid @RequestBody UnifiedRequest<CreateCategoryReqVO> req) {
        Long userId = UserContext.getUserId();
        CategoryRespVO respVO = categoryService.createCategory(userId, req.getData());
        return Result.success("创建成功", respVO);
    }

    @PostMapping("/update")
    @OperLog("更新类别")
    @Operation(summary = "更新类别", description = "更新指定的账单类别")
    public Result<CategoryRespVO> update(@Valid @RequestBody UnifiedRequest<UpdateCategoryReqVO> req) {
        Long userId = UserContext.getUserId();
        CategoryRespVO respVO = categoryService.updateCategory(userId, req.getData());
        return Result.success("更新成功", respVO);
    }

    @PostMapping("/delete")
    @OperLog("删除类别")
    @Operation(summary = "删除类别", description = "删除指定的账单类别")
    public Result<Void> delete(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        categoryService.deleteCategory(userId, req.getData().getId());
        return Result.success("删除成功");
    }
}
