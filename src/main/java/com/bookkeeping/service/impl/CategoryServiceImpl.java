package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.entity.Category;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.req.CategoryListReqVO;
import com.bookkeeping.vo.req.CreateCategoryReqVO;
import com.bookkeeping.vo.req.UpdateCategoryReqVO;
import com.bookkeeping.vo.resp.CategoryRespVO;
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

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_CATEGORY_KEY = "category:default";
    private static final String USER_CATEGORY_KEY_PREFIX = "category:user:";

    @Override
    public List<CategoryRespVO> getCategoryList(Long userId, CategoryListReqVO reqVO) {
        List<CategoryRespVO> result = new ArrayList<>();

        List<CategoryRespVO> defaultCategories = getDefaultCategories();
        result.addAll(filterByType(defaultCategories, reqVO != null ? reqVO.getType() : null));

        List<CategoryRespVO> userCategories = getUserCategories(userId);
        result.addAll(filterByType(userCategories, reqVO != null ? reqVO.getType() : null));

        return result;
    }

    private List<CategoryRespVO> getDefaultCategories() {
        Object cached = redisUtil.get(DEFAULT_CATEGORY_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached.toString(), new TypeReference<List<CategoryRespVO>>() {});
            } catch (Exception e) {
                // 缓存解析失败，重新加载
            }
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, 0);
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        List<CategoryRespVO> voList = categories.stream().map(this::convertToRespVO).collect(Collectors.toList());

        try {
            redisUtil.set(DEFAULT_CATEGORY_KEY, objectMapper.writeValueAsString(voList), 24, TimeUnit.HOURS);
        } catch (Exception e) {
            // 忽略缓存失败
        }

        return voList;
    }

    private List<CategoryRespVO> getUserCategories(Long userId) {
        String key = USER_CATEGORY_KEY_PREFIX + userId;
        Object cached = redisUtil.get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached.toString(), new TypeReference<List<CategoryRespVO>>() {});
            } catch (Exception e) {
                // 缓存解析失败，重新加载
            }
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        List<CategoryRespVO> voList = categories.stream().map(this::convertToRespVO).collect(Collectors.toList());

        try {
            redisUtil.set(key, objectMapper.writeValueAsString(voList), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // 忽略缓存失败
        }

        return voList;
    }

    private List<CategoryRespVO> filterByType(List<CategoryRespVO> list, Integer type) {
        if (type == null) {
            return list;
        }
        return list.stream().filter(c -> c.getType().equals(type)).collect(Collectors.toList());
    }

    private void clearUserCategoryCache(Long userId) {
        redisUtil.delete(USER_CATEGORY_KEY_PREFIX + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryRespVO createCategory(Long userId, CreateCategoryReqVO reqVO) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        wrapper.eq(Category::getName, reqVO.getName());
        wrapper.eq(Category::getType, reqVO.getType());
        wrapper.eq(Category::getDeleted, 0);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该类别已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(reqVO, category);
        category.setUserId(userId);
        category.setIsDefault(0);

        categoryMapper.insert(category);
        clearUserCategoryCache(userId);
        return convertToRespVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryRespVO updateCategory(Long userId, UpdateCategoryReqVO reqVO) {
        Category category = categoryMapper.selectById(reqVO.getId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        if (category.getUserId() == 0 || !category.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该类别");
        }

        BeanUtils.copyProperties(reqVO, category, "id", "userId", "createTime", "isDefault");
        categoryMapper.updateById(category);
        clearUserCategoryCache(userId);
        return convertToRespVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        if (category.getUserId() == 0 || !category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该类别");
        }

        categoryMapper.deleteById(categoryId);
        clearUserCategoryCache(userId);
    }

    private CategoryRespVO convertToRespVO(Category category) {
        return CategoryRespVO.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .sort(category.getSortOrder())
                .createTime(category.getCreateTime() != null ? category.getCreateTime().toString() : null)
                .build();
    }
}
