package com.bookkeeping.service;

import com.bookkeeping.vo.req.CategoryListReqVO;
import com.bookkeeping.vo.req.CreateCategoryReqVO;
import com.bookkeeping.vo.req.UpdateCategoryReqVO;
import com.bookkeeping.vo.resp.CategoryRespVO;

import java.util.List;

public interface CategoryService {

    List<CategoryRespVO> getCategoryList(Long userId, CategoryListReqVO reqVO);

    CategoryRespVO createCategory(Long userId, CreateCategoryReqVO reqVO);

    CategoryRespVO updateCategory(Long userId, UpdateCategoryReqVO reqVO);

    void deleteCategory(Long userId, Long categoryId);
}
