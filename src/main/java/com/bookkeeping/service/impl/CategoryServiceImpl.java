package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.dto.CategoryDTO;
import com.bookkeeping.entity.Category;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.CategoryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 类别服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    // 系统默认类别缓存key
    private static final String DEFAULT_CATEGORY_KEY = "category:default";
    // 用户自定义类别缓存key前缀
    private static final String USER_CATEGORY_KEY_PREFIX = "category:user:";

    @Override
    public List<CategoryVO> getCategoryList(Long userId, Integer type) {
        List<CategoryVO> result = new ArrayList<>();

        // 获取并缓存系统默认类别
        List<CategoryVO> defaultCategories = getDefaultCategories();
        result.addAll(filterByType(defaultCategories, type));

        // 获取并缓存用户自定义类别
        List<CategoryVO> userCategories = getUserCategories(userId);
        result.addAll(filterByType(userCategories, type));

        return result;
    }

    /**
     * 获取系统默认类别（带缓存）
     */
    private List<CategoryVO> getDefaultCategories() {
        Object cached = redisUtil.get(DEFAULT_CATEGORY_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached.toString(), new TypeReference<List<CategoryVO>>() {});
            } catch (Exception e) {
                // 缓存解析失败，重新加载
            }
        }

        // 从数据库加载
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, 0);
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        List<CategoryVO> voList = categories.stream().map(this::convertToVO).collect(Collectors.toList());

        // 缓存24小时
        try {
            redisUtil.set(DEFAULT_CATEGORY_KEY, objectMapper.writeValueAsString(voList), 24, TimeUnit.HOURS);
        } catch (Exception e) {
            // 忽略缓存失败
        }

        return voList;
    }

    /**
     * 获取用户自定义类别（带缓存）
     */
    private List<CategoryVO> getUserCategories(Long userId) {
        String key = USER_CATEGORY_KEY_PREFIX + userId;
        Object cached = redisUtil.get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached.toString(), new TypeReference<List<CategoryVO>>() {});
            } catch (Exception e) {
                // 缓存解析失败，重新加载
            }
        }

        // 从数据库加载
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        List<CategoryVO> voList = categories.stream().map(this::convertToVO).collect(Collectors.toList());

        // 缓存1小时
        try {
            redisUtil.set(key, objectMapper.writeValueAsString(voList), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // 忽略缓存失败
        }

        return voList;
    }

    /**
     * 按类型过滤
     */
    private List<CategoryVO> filterByType(List<CategoryVO> list, Integer type) {
        if (type == null) {
            return list;
        }
        return list.stream().filter(c -> c.getType().equals(type)).collect(Collectors.toList());
    }

    /**
     * 清除用户类别缓存
     */
    private void clearUserCategoryCache(Long userId) {
        redisUtil.delete(USER_CATEGORY_KEY_PREFIX + userId);
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
        clearUserCategoryCache(userId);
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
        clearUserCategoryCache(userId);
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
        clearUserCategoryCache(userId);
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
