package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.dto.CategoryDTO;
import com.bookkeeping.entity.Category;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 类别服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getCategoryList(Long userId, Integer type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Category::getUserId, 0).or().eq(Category::getUserId, userId));
        if (type != null) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);

        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO createCategory(Long userId, CategoryDTO categoryDTO) {
        // 检查是否已存在同名类别
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        wrapper.eq(Category::getName, categoryDTO.getName());
        wrapper.eq(Category::getType, categoryDTO.getType());
        wrapper.eq(Category::getDeleted, 0);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该类别已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUserId(userId);
        category.setIsDefault(0);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        categoryMapper.insert(category);
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        // 只能修改自己创建的类别
        if (category.getUserId() == 0 || !category.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该类别");
        }

        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        // 只能删除自己创建的类别
        if (category.getUserId() == 0 || !category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该类别");
        }

        categoryMapper.deleteById(categoryId);
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
