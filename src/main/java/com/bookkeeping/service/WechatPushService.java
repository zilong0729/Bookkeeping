package com.bookkeeping.service;

import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.User;
import com.bookkeeping.vo.ContactVO;

import java.util.List;

public interface WechatPushService {
    void pushEventNotification(MyEvent event, User user, List<ContactVO> contacts);
}
