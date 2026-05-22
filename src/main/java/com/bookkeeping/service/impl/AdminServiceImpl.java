package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public LoginRespVO adminLogin(AdminLoginReqVO reqVO) {
        // 检查登录失败次数
        String loginFailKey = "login_fail:admin:" + reqVO.getUsername();
        Object failCountObj = redisUtil.get(loginFailKey);
        int failCount = failCountObj != null ? Integer.parseInt(failCountObj.toString()) : 0;
        
        if (failCount >= 5) {
            throw new BusinessException("登录失败次数过多，请15分钟后再试");
        }

        // 校验管理员账号密码
        boolean loginSuccess = true;
        if (!adminUsername.equals(reqVO.getUsername())) {
            loginSuccess = false;
        }
        if (!adminPassword.equals(reqVO.getPassword())) {
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
        LoginRespVO respVO = new LoginRespVO();
        respVO.setToken(token);
        respVO.setExpiresIn(7 * 24 * 60 * 60L);
        respVO.setUserInfo(convertToUserRespVO(adminUser));

        return respVO;
    }

    @Override
    public PageResult<UserRespVO> listUsers(AdminUserQueryReqVO reqVO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        if (StringUtils.hasText(reqVO.getKeyword())) {
            wrapper.and(w -> w.like(User::getNickname, reqVO.getKeyword())
                    .or().like(User::getPhone, reqVO.getKeyword()));
        }
        if (reqVO.getStatus() != null) {
            wrapper.eq(User::getStatus, reqVO.getStatus());
        }

        wrapper.orderByDesc(User::getCreateTime);

        Long current = reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO.getSize() != null ? reqVO.getSize() : 10L;
        
        Page<User> page = new Page<>(current, size);
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<UserRespVO> records = userPage.getRecords().stream()
                .map(this::convertToUserRespVO)
                .collect(Collectors.toList());

        return PageResult.of(records, userPage.getTotal(), userPage.getCurrent(), userPage.getSize());
    }

    @Override
    public UserRespVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserRespVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRespVO operateUser(AdminOperateUserReqVO reqVO) {
        User user = userMapper.selectById(reqVO.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        if (reqVO.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (reqVO.getOperation() != null) {
            user.setStatus(reqVO.getOperation());
        }

        userMapper.updateById(user);

        // 如果禁用了用户，清除其token强制下线
        if (reqVO.getOperation() != null && reqVO.getOperation() == 0) {
            redisUtil.deleteUserToken(reqVO.getUserId());
        }

        return convertToUserRespVO(user);
    }

    @Override
    public PageResult<RecordRespVO> listUserRecords(AdminUserRecordsReqVO reqVO) {
        User user = userMapper.selectById(reqVO.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, reqVO.getUserId());
        wrapper.eq(Record::getDeleted, 0);

        if (reqVO.getType() != null) {
            wrapper.eq(Record::getType, reqVO.getType());
        }
        if (StringUtils.hasText(reqVO.getStartDate())) {
            wrapper.ge(Record::getRecordDate, LocalDate.parse(reqVO.getStartDate()));
        }
        if (StringUtils.hasText(reqVO.getEndDate())) {
            wrapper.le(Record::getRecordDate, LocalDate.parse(reqVO.getEndDate()));
        }

        wrapper.orderByDesc(Record::getRecordDate);
        wrapper.orderByDesc(Record::getCreateTime);

        Long current = reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO.getSize() != null ? reqVO.getSize() : 10L;
        
        Page<Record> page = new Page<>(current, size);
        Page<Record> recordPage = recordMapper.selectPage(page, wrapper);

        List<RecordRespVO> records = recordPage.getRecords().stream()
                .map(r -> {
                    Category category = categoryMapper.selectById(r.getCategoryId());
                    return convertToRecordRespVO(r, category);
                })
                .collect(Collectors.toList());

        return PageResult.of(records, recordPage.getTotal(), recordPage.getCurrent(), recordPage.getSize());
    }

    @Override
    public StatisticsRespVO getUserStatistics(AdminUserStatisticsReqVO reqVO) {
        User user = userMapper.selectById(reqVO.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        LocalDate start = StringUtils.hasText(reqVO.getStartDate()) ? LocalDate.parse(reqVO.getStartDate()) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = StringUtils.hasText(reqVO.getEndDate()) ? LocalDate.parse(reqVO.getEndDate()) : LocalDate.now();

        BigDecimal totalIncome = recordMapper.sumAmountByDateRange(reqVO.getUserId(), 1, start, end);
        BigDecimal totalExpense = recordMapper.sumAmountByDateRange(reqVO.getUserId(), 2, start, end);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        return StatisticsRespVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }

    @Override
    public PageResult<OperationLogRespVO> listLogs(LogQueryReqVO reqVO) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (reqVO.getOperatorId() != null) {
            wrapper.eq(OperationLog::getUserId, reqVO.getOperatorId());
        }
        if (StringUtils.hasText(reqVO.getKeyword())) {
            wrapper.like(OperationLog::getOperation, reqVO.getKeyword());
        }
        if (StringUtils.hasText(reqVO.getStartDate())) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(reqVO.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(reqVO.getEndDate())) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(reqVO.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        Long current = reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO.getSize() != null ? reqVO.getSize() : 10L;
        
        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);

        List<OperationLogRespVO> records = logPage.getRecords().stream()
                .map(this::convertToOperationLogRespVO)
                .collect(Collectors.toList());

        return PageResult.of(records, logPage.getTotal(), logPage.getCurrent(), logPage.getSize());
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

    private UserRespVO convertToUserRespVO(User user) {
        return UserRespVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .status(user.getStatus())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }

    private RecordRespVO convertToRecordRespVO(Record record, Category category) {
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

    private OperationLogRespVO convertToOperationLogRespVO(OperationLog log) {
        return OperationLogRespVO.builder()
                .id(log.getId())
                .operatorId(log.getUserId())
                .operatorName(log.getUsername())
                .operation(log.getOperation())
                .method(log.getMethod())
                .requestUrl(log.getRequestUrl())
                .requestMethod(log.getRequestMethod())
                .requestParams(log.getRequestParams())
                .responseResult(log.getResponseResult())
                .ip(log.getIp())
                .status(log.getStatus())
                .errorMsg(log.getErrorMsg())
                .executionTime(log.getExecutionTime())
                .createTime(log.getCreateTime() != null ? log.getCreateTime().toString() : null)
                .build();
    }
}
