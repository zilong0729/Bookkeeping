package com.bookkeeping.service;

import com.bookkeeping.dto.CategoryDTO;
import com.bookkeeping.vo.CategoryVO;

import java.util.List;

/**
 * 类别服务接口
 */
public interface CategoryService {

    /**
     * 获取用户的类别列表
     */
    List<CategoryVO> getCategoryList(Long userId, Integer type);

    /**
     * 创建类别
     */
    CategoryVO createCategory(Long userId, CategoryDTO categoryDTO);

    /**
     * 更新类别
     */
    CategoryVO updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO);

    /**
     * 删除类别
     */
    void deleteCategory(Long userId, Long categoryId);
}
