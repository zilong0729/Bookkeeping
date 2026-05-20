package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.dto.RecordDTO;
import com.bookkeeping.dto.RecordQueryDTO;
import com.bookkeeping.entity.Category;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.CacheService;
import com.bookkeeping.service.RecordService;
import com.bookkeeping.vo.RecordVO;
import com.bookkeeping.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;
    private final CacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordVO createRecord(Long userId, RecordDTO recordDTO) {
        // 验证类别是否存在
        Category category = categoryMapper.selectById(recordDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        // 验证类别类型是否匹配
        if (!category.getType().equals(recordDTO.getType())) {
            throw new BusinessException("类别类型不匹配");
        }

        Record record = new Record();
        BeanUtils.copyProperties(recordDTO, record);
        record.setUserId(userId);

        recordMapper.insert(record);

        // 清除用户统计缓存（账单变动，统计失效）
        cacheService.evictStatistics(userId);

        return convertToVO(record, category.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordVO updateRecord(Long userId, Long recordId, RecordDTO recordDTO) {
        Record record = recordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        // 只能修改自己的账单
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该账单");
        }

        // 验证类别是否存在
        Category category = categoryMapper.selectById(recordDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        // 验证类别类型是否匹配
        if (!category.getType().equals(recordDTO.getType())) {
            throw new BusinessException("类别类型不匹配");
        }

        BeanUtils.copyProperties(recordDTO, record);
        recordMapper.updateById(record);

        // 清除用户统计缓存
        cacheService.evictStatistics(userId);

        return convertToVO(record, category.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long userId, Long recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        // 只能删除自己的账单
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该账单");
        }

        recordMapper.deleteById(recordId);

        // 清除用户统计缓存
        cacheService.evictStatistics(userId);
    }

    @Override
    public RecordVO getRecordDetail(Long userId, Long recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        // 只能查看自己的账单
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该账单");
        }

        Category category = categoryMapper.selectById(record.getCategoryId());
        String categoryName = category != null ? category.getName() : "";
        return convertToVO(record, categoryName);
    }

    @Override
    public PageResult<RecordVO> getRecordList(Long userId, RecordQueryDTO queryDTO) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getDeleted, 0);

        // 类型筛选
        if (queryDTO.getType() != null) {
            wrapper.eq(Record::getType, queryDTO.getType());
        }

        // 类别筛选
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Record::getCategoryId, queryDTO.getCategoryId());
        }

        // 日期范围筛选
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(Record::getRecordDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(Record::getRecordDate, queryDTO.getEndDate());
        }

        // 往来对象姓名模糊查询
        if (StringUtils.hasText(queryDTO.getContactName())) {
            wrapper.like(Record::getContactName, queryDTO.getContactName());
        }

        // 按日期倒序排列
        wrapper.orderByDesc(Record::getRecordDate);
        wrapper.orderByDesc(Record::getCreateTime);

        Page<Record> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Record> recordPage = recordMapper.selectPage(page, wrapper);

        List<RecordVO> records = recordPage.getRecords().stream()
                .map(r -> {
                    Category category = categoryMapper.selectById(r.getCategoryId());
                    String categoryName = category != null ? category.getName() : "";
                    return convertToVO(r, categoryName);
                })
                .collect(Collectors.toList());

        Page<RecordVO> resultPage = new Page<>();
        resultPage.setCurrent(recordPage.getCurrent());
        resultPage.setSize(recordPage.getSize());
        resultPage.setTotal(recordPage.getTotal());
        resultPage.setPages(recordPage.getPages());
        resultPage.setRecords(records);

        return PageResult.from(resultPage);
    }

    @Override
    public StatisticsVO getStatistics(Long userId, RecordQueryDTO queryDTO) {
        LocalDate startDate = queryDTO.getStartDate();
        LocalDate endDate = queryDTO.getEndDate();

        // 如果没有指定日期范围，默认统计当月
        if (startDate == null || endDate == null) {
            YearMonth now = YearMonth.now();
            startDate = now.atDay(1);
            endDate = now.atEndOfMonth();
        }

        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;

        // 尝试从缓存获取统计数据
        BigDecimal cachedIncome = cacheService.getStatistics(userId, 1, finalStartDate, finalEndDate);
        BigDecimal cachedExpense = cacheService.getStatistics(userId, 2, finalStartDate, finalEndDate);

        if (cachedIncome != null && cachedExpense != null) {
            log.debug("统计缓存命中: userId={}", userId);
            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setTotalIncome(cachedIncome);
            statisticsVO.setTotalExpense(cachedExpense);
            statisticsVO.setBalance(cachedIncome.subtract(cachedExpense));
            return statisticsVO;
        }

        // 缓存未命中，从数据库查询
        BigDecimal totalIncome = recordMapper.sumAmountByDateRange(userId, 1, finalStartDate, finalEndDate);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpense = recordMapper.sumAmountByDateRange(userId, 2, finalStartDate, finalEndDate);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        // 写入缓存
        cacheService.cacheStatistics(userId, 1, finalStartDate, finalEndDate, totalIncome);
        cacheService.cacheStatistics(userId, 2, finalStartDate, finalEndDate, totalExpense);

        StatisticsVO statisticsVO = new StatisticsVO();
        statisticsVO.setTotalIncome(totalIncome);
        statisticsVO.setTotalExpense(totalExpense);
        statisticsVO.setBalance(totalIncome.subtract(totalExpense));

        return statisticsVO;
    }

    private RecordVO convertToVO(Record record, String categoryName) {
        RecordVO vo = new RecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setCategoryName(categoryName);
        return vo;
    }
}
