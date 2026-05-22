package com.bookkeeping.service;

import com.bookkeeping.entity.User;
import com.bookkeeping.vo.ContactVO;
import com.bookkeeping.vo.MyEventVO;

import java.util.List;

public interface WechatPushService {
    void pushEventNotification(MyEventVO event, User user, List<ContactVO> contacts);
}
