package com.bookkeeping.service;

import com.bookkeeping.vo.req.LoginReqVO;
import com.bookkeeping.vo.req.UserUpdateReqVO;
import com.bookkeeping.vo.resp.LoginRespVO;
import com.bookkeeping.vo.resp.UserRespVO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserService {

    LoginRespVO wxLogin(LoginReqVO loginVO) throws JsonProcessingException;

    UserRespVO getCurrentUser(Long userId);

    UserRespVO updateUser(Long userId, UserUpdateReqVO updateVO);

    void logout(Long userId);

    void forceLogout(Long userId);
}
