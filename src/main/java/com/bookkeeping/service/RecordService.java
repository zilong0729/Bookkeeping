package com.bookkeeping.service;

import com.bookkeeping.vo.req.CreateRecordReqVO;
import com.bookkeeping.vo.req.RecordListReqVO;
import com.bookkeeping.vo.req.StatisticsReqVO;
import com.bookkeeping.vo.req.UpdateRecordReqVO;
import com.bookkeeping.vo.resp.RecordRespVO;
import com.bookkeeping.vo.resp.StatisticsRespVO;

import java.util.List;

public interface RecordService {

    RecordRespVO createRecord(Long userId, CreateRecordReqVO reqVO);

    RecordRespVO updateRecord(Long userId, UpdateRecordReqVO reqVO);

    void deleteRecord(Long userId, Long recordId);

    RecordRespVO getRecordDetail(Long userId, Long recordId);

    List<RecordRespVO> getRecordList(Long userId, RecordListReqVO reqVO);

    StatisticsRespVO getStatistics(Long userId, StatisticsReqVO reqVO);
}
