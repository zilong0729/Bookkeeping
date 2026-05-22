package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.dto.*;
import com.bookkeeping.entity.Category;
import com.bookkeeping.entity.OperationLog;
import com.bookkeeping.entity.Record;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.mapper.OperationLogMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.AdminService;
import com.bookkeeping.utils.JwtUtil;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;
    private final OperationLogMapper operationLogMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;


    @Override
    public LoginVO adminLogin(AdminLoginDTO loginDTO) {
        // 检查登录失败次数
        String loginFailKey = "login_fail:admin:" + loginDTO.getUsername();
        Object failCountObj = redisUtil.get(loginFailKey);
        int failCount = failCountObj != null ? Integer.parseInt(failCountObj.toString()) : 0;
        
        if (failCount >= 5) {
            throw new BusinessException("登录失败次数过多，请15分钟后再试");
        }

        // 校验管理员账号密码
        boolean loginSuccess = true;
        if (!adminUsername.equals(loginDTO.getUsername())) {
            loginSuccess = false;
        }
        if (!adminPassword.equals(loginDTO.getPassword())) {
            loginSuccess = false;
        }

        if (!loginSuccess) {
            // 记录失败次数
            failCount++;
            redisUtil.set(loginFailKey, failCount, 15, TimeUnit.MINUTES);
            throw new BusinessException("用户名或密码错误，剩余尝试次数：" + (5 - failCount));
        }

        // 登录成功，清除失败计数
        redisUtil.delete(loginFailKey);

        // 查找或创建管理员用户记录
        User adminUser = findOrCreateAdmin();

        // 生成token（role=1）
        String token = jwtUtil.generateToken(adminUser.getId(), "admin", 1);

        // 缓存token
        redisUtil.cacheUserToken(adminUser.getId(), token, 7, TimeUnit.DAYS);

        // 组装返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(7 * 24 * 60 * 60L);
        loginVO.setUserInfo(convertToUserVO(adminUser));

        return loginVO;
    }

    @Override
    public PageResult<UserVO> listUsers(AdminUserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        if (StringUtils.hasText(queryDTO.getNickname())) {
            wrapper.like(User::getNickname, queryDTO.getNickname());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(User::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getRole() != null) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<UserVO> records = userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());

        Page<UserVO> resultPage = new Page<>();
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setTotal(userPage.getTotal());
        resultPage.setPages(userPage.getPages());
        resultPage.setRecords(records);

        return PageResult.from(resultPage);
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO operateUser(Long targetUserId, AdminOperateUserDTO operateDTO) {
        User user = userMapper.selectById(targetUserId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        if (operateDTO.getStatus() != null) {
            user.setStatus(operateDTO.getStatus());
        }
        if (operateDTO.getRole() != null) {
            user.setRole(operateDTO.getRole());
        }

        userMapper.updateById(user);

        // 如果禁用了用户，清除其token强制下线
        if (operateDTO.getStatus() != null && operateDTO.getStatus() == 0) {
            redisUtil.deleteUserToken(targetUserId);
        }

        return convertToUserVO(user);
    }

    @Override
    public PageResult<?> listUserRecords(Long targetUserId, Integer type,
                                         String startDate, String endDate,
                                         Long current, Long size) {
        User user = userMapper.selectById(targetUserId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, targetUserId);
        wrapper.eq(Record::getDeleted, 0);

        if (type != null) {
            wrapper.eq(Record::getType, type);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Record::getRecordDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Record::getRecordDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(Record::getRecordDate);
        wrapper.orderByDesc(Record::getCreateTime);

        Page<Record> page = new Page<>(current, size);
        Page<Record> recordPage = recordMapper.selectPage(page, wrapper);

        List<RecordVO> records = recordPage.getRecords().stream()
                .map(r -> {
                    Category category = categoryMapper.selectById(r.getCategoryId());
                    String categoryName = category != null ? category.getName() : "";
                    RecordVO vo = new RecordVO();
                    BeanUtils.copyProperties(r, vo);
                    vo.setCategoryName(categoryName);
                    return vo;
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
    public Object getUserStatistics(Long targetUserId, String startDate, String endDate) {
        User user = userMapper.selectById(targetUserId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        LocalDate start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.now();

        BigDecimal totalIncome = recordMapper.sumAmountByDateRange(targetUserId, 1, start, end);
        BigDecimal totalExpense = recordMapper.sumAmountByDateRange(targetUserId, 2, start, end);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        StatisticsVO vo = new StatisticsVO();
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));
        return vo;
    }

    @Override
    public PageResult<OperationLogVO> listLogs(LogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(OperationLog::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getOperation())) {
            wrapper.like(OperationLog::getOperation, queryDTO.getOperation());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(OperationLog::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(queryDTO.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(queryDTO.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);

        List<OperationLogVO> records = logPage.getRecords().stream()
                .map(log -> {
                    OperationLogVO vo = new OperationLogVO();
                    BeanUtils.copyProperties(log, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        Page<OperationLogVO> resultPage = new Page<>();
        resultPage.setCurrent(logPage.getCurrent());
        resultPage.setSize(logPage.getSize());
        resultPage.setTotal(logPage.getTotal());
        resultPage.setPages(logPage.getPages());
        resultPage.setRecords(records);

        return PageResult.from(resultPage);
    }

    /**
     * 查找或创建管理员用户
     */
    private User findOrCreateAdmin() {
        // 尝试通过openid查找管理员
        User user = userMapper.selectByOpenid("admin");
        if (user != null) {
            return user;
        }

        // 创建管理员用户
        user = new User();
        user.setOpenid("admin");
        user.setNickname("系统管理员");
        user.setStatus(1);
        user.setRole(1);
        userMapper.insert(user);
        return user;
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
