package com.bookkeeping.service;

import com.bookkeeping.common.PageResult;
import com.bookkeeping.dto.AdminLoginDTO;
import com.bookkeeping.dto.AdminOperateUserDTO;
import com.bookkeeping.dto.AdminUserQueryDTO;
import com.bookkeeping.dto.LogQueryDTO;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.OperationLogVO;
import com.bookkeeping.vo.UserVO;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    LoginVO adminLogin(AdminLoginDTO loginDTO);

    /**
     * 分页查询所有用户
     */
    PageResult<UserVO> listUsers(AdminUserQueryDTO queryDTO);

    /**
     * 查看指定用户详情
     */
    UserVO getUserDetail(Long userId);

    /**
     * 操作用户（禁用/启用、设置角色）
     */
    UserVO operateUser(Long targetUserId, AdminOperateUserDTO operateDTO);

    /**
     * 查看指定用户的账单列表
     */
    PageResult<?> listUserRecords(Long targetUserId, Integer type,
                                  String startDate, String endDate,
                                  Long current, Long size);

    /**
     * 查看指定用户的统计信息
     */
    Object getUserStatistics(Long targetUserId, String startDate, String endDate);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogVO> listLogs(LogQueryDTO queryDTO);
}
