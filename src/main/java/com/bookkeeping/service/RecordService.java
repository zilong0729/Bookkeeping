package com.bookkeeping.service;

import com.bookkeeping.common.PageResult;
import com.bookkeeping.dto.RecordDTO;
import com.bookkeeping.dto.RecordQueryDTO;
import com.bookkeeping.vo.RecordVO;
import com.bookkeeping.vo.StatisticsVO;

/**
 * 账单服务接口
 */
public interface RecordService {

    /**
     * 创建账单
     */
    RecordVO createRecord(Long userId, RecordDTO recordDTO);

    /**
     * 更新账单
     */
    RecordVO updateRecord(Long userId, Long recordId, RecordDTO recordDTO);

    /**
     * 删除账单
     */
    void deleteRecord(Long userId, Long recordId);

    /**
     * 获取账单详情
     */
    RecordVO getRecordDetail(Long userId, Long recordId);

    /**
     * 查询账单列表
     */
    PageResult<RecordVO> getRecordList(Long userId, RecordQueryDTO queryDTO);

    /**
     * 统计账单金额
     */
    StatisticsVO getStatistics(Long userId, RecordQueryDTO queryDTO);
}
