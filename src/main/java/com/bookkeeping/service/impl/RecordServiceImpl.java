package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.entity.Category;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.CacheService;
import com.bookkeeping.service.RecordService;
import com.bookkeeping.vo.req.CreateRecordReqVO;
import com.bookkeeping.vo.req.RecordListReqVO;
import com.bookkeeping.vo.req.StatisticsReqVO;
import com.bookkeeping.vo.req.UpdateRecordReqVO;
import com.bookkeeping.vo.resp.RecordRespVO;
import com.bookkeeping.vo.resp.StatisticsRespVO;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;
    private final CacheService cacheService;
    private final ContactMapper contactMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordRespVO createRecord(Long userId, CreateRecordReqVO reqVO) {
        Category category = categoryMapper.selectById(reqVO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("类别不存在");
        }

        if (!category.getType().equals(reqVO.getType())) {
            throw new BusinessException("类别类型不匹配");
        }

        Record record = new Record();
        BeanUtils.copyProperties(reqVO, record);
        record.setUserId(userId);

        if (reqVO.getContactId() != null) {
            Contact contact = contactMapper.selectById(reqVO.getContactId());
            if (contact != null && contact.getDeleted() == 0 && contact.getUserId().equals(userId)) {
                record.setContactName(contact.getName());
            }
        }

        recordMapper.insert(record);
        cacheService.evictStatistics(userId);

        return convertToRespVO(record, category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordRespVO updateRecord(Long userId, UpdateRecordReqVO reqVO) {
        Record record = recordMapper.selectById(reqVO.getId());
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该账单");
        }

        if (reqVO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(reqVO.getCategoryId());
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException("类别不存在");
            }
            if (!category.getType().equals(reqVO.getType())) {
                throw new BusinessException("类别类型不匹配");
            }
        }

        BeanUtils.copyProperties(reqVO, record, "id", "userId", "createTime");

        if (reqVO.getContactId() != null) {
            Contact contact = contactMapper.selectById(reqVO.getContactId());
            if (contact != null && contact.getDeleted() == 0 && contact.getUserId().equals(userId)) {
                record.setContactName(contact.getName());
            }
        }

        recordMapper.updateById(record);
        cacheService.evictStatistics(userId);

        Category category = categoryMapper.selectById(record.getCategoryId());
        return convertToRespVO(record, category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long userId, Long recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该账单");
        }

        recordMapper.deleteById(recordId);
        cacheService.evictStatistics(userId);
    }

    @Override
    public RecordRespVO getRecordDetail(Long userId, Long recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("账单不存在");
        }

        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该账单");
        }

        Category category = categoryMapper.selectById(record.getCategoryId());
        return convertToRespVO(record, category);
    }

    @Override
    public List<RecordRespVO> getRecordList(Long userId, RecordListReqVO reqVO) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getDeleted, 0);

        if (reqVO.getType() != null) {
            wrapper.eq(Record::getType, reqVO.getType());
        }

        if (reqVO.getCategoryId() != null) {
            wrapper.eq(Record::getCategoryId, reqVO.getCategoryId());
        }

        if (reqVO.getStartDate() != null) {
            wrapper.ge(Record::getRecordDate, reqVO.getStartDate());
        }
        if (reqVO.getEndDate() != null) {
            wrapper.le(Record::getRecordDate, reqVO.getEndDate());
        }

        if (StringUtils.hasText(reqVO.getContactName())) {
            wrapper.like(Record::getContactName, reqVO.getContactName());
        }

        wrapper.orderByDesc(Record::getRecordDate);
        wrapper.orderByDesc(Record::getCreateTime);

        List<Record> records = recordMapper.selectList(wrapper);

        return records.stream()
                .map(r -> {
                    Category category = categoryMapper.selectById(r.getCategoryId());
                    return convertToRespVO(r, category);
                })
                .collect(Collectors.toList());
    }

    @Override
    public StatisticsRespVO getStatistics(Long userId, StatisticsReqVO reqVO) {
        LocalDate startDate = reqVO.getStartDate();
        LocalDate endDate = reqVO.getEndDate();

        if (startDate == null || endDate == null) {
            YearMonth now = YearMonth.now();
            startDate = now.atDay(1);
            endDate = now.atEndOfMonth();
        }

        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;

        BigDecimal cachedIncome = cacheService.getStatistics(userId, 1, finalStartDate, finalEndDate);
        BigDecimal cachedExpense = cacheService.getStatistics(userId, 2, finalStartDate, finalEndDate);

        if (cachedIncome != null && cachedExpense != null) {
            log.debug("统计缓存命中: userId={}", userId);
            return StatisticsRespVO.builder()
                    .totalIncome(cachedIncome)
                    .totalExpense(cachedExpense)
                    .balance(cachedIncome.subtract(cachedExpense))
                    .build();
        }

        BigDecimal totalIncome = recordMapper.sumAmountByDateRange(userId, 1, finalStartDate, finalEndDate);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpense = recordMapper.sumAmountByDateRange(userId, 2, finalStartDate, finalEndDate);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        cacheService.cacheStatistics(userId, 1, finalStartDate, finalEndDate, totalIncome);
        cacheService.cacheStatistics(userId, 2, finalStartDate, finalEndDate, totalExpense);

        return StatisticsRespVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }

    private RecordRespVO convertToRespVO(Record record, Category category) {
        return RecordRespVO.builder()
                .id(record.getId())
                .categoryId(record.getCategoryId())
                .categoryName(category != null ? category.getName() : "")
                .categoryIcon(category != null ? category.getIcon() : "")
                .type(record.getType())
                .amount(record.getAmount())
                .contactId(record.getContactId())
                .contactName(record.getContactName())
                .recordDate(record.getRecordDate())
                .remark(record.getRemark())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }
}
