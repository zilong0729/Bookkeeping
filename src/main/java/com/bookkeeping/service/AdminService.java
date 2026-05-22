package com.bookkeeping.service;

import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    LoginRespVO adminLogin(AdminLoginReqVO reqVO);

    /**
     * 分页查询所有用户
     */
    PageResult<UserRespVO> listUsers(AdminUserQueryReqVO reqVO);

    /**
     * 查看指定用户详情
     */
    UserRespVO getUserDetail(Long userId);

    /**
     * 操作用户（禁用/启用）
     */
    UserRespVO operateUser(AdminOperateUserReqVO reqVO);

    /**
     * 查看指定用户的账单列表
     */
    PageResult<RecordRespVO> listUserRecords(AdminUserRecordsReqVO reqVO);

    /**
     * 查看指定用户的统计信息
     */
    StatisticsRespVO getUserStatistics(AdminUserStatisticsReqVO reqVO);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogRespVO> listLogs(LogQueryReqVO reqVO);
}
