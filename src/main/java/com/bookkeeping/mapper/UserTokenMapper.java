package com.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookkeeping.entity.UserToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户TokenMapper
 */
@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {

    /**
     * 根据token查询
     */
    UserToken selectByToken(@Param("token") String token);

    /**
     * 根据用户ID查询所有有效token
     */
    List<UserToken> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID统计登录次数
     */
    Integer countLoginByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID失效所有token
     */
    int invalidateByUserId(@Param("userId") Long userId);

    /**
     * 根据设备ID查询token
     */
    UserToken selectByDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 更新最后活跃时间
     */
    int updateLastActiveTime(@Param("id") Long id, @Param("lastActiveTime") LocalDateTime lastActiveTime);
}
