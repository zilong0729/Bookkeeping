package com.bookkeeping.service.impl;

import com.bookkeeping.config.WechatConfig;
import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.User;
import com.bookkeeping.service.WechatPushService;
import com.bookkeeping.utils.WechatUtil;
import com.bookkeeping.vo.ContactVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPushServiceImpl implements WechatPushService {

    private final WechatUtil wechatUtil;
    private final WechatConfig wechatConfig;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Override
    public void pushEventNotification(MyEvent event, User user, List<ContactVO> contacts) {
        log.info("开始推送事件通知: eventId={}, eventTitle={}, user={}", event.getId(), event.getTitle(), user.getNickname());

        String eventTypeText = getEventTypeText(event.getEventType());
        String userName = user.getNickname() != null ? user.getNickname() : "您的朋友";

        if (contacts == null || contacts.isEmpty()) {
            log.info("没有需要推送的联系人，跳过推送");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (ContactVO contact : contacts) {
            if (contact.getOpenId() == null || contact.getOpenId().isEmpty()) {
                log.warn("联系人未绑定微信OpenID，跳过推送: contactId={}, contactName={}", contact.getId(), contact.getName());
                failCount++;
                continue;
            }

            try {
                Map<String, String> data = buildMessageData(event, userName, eventTypeText);
                boolean success = wechatUtil.sendSubscribeMessage(contact.getOpenId(), wechatConfig.getTemplateId(), data);
                
                if (success) {
                    log.info("推送通知成功: contactId={}, contactName={}, openId={}", contact.getId(), contact.getName(), contact.getOpenId());
                    successCount++;
                } else {
                    log.warn("推送通知失败: contactId={}, contactName={}", contact.getId(), contact.getName());
                    failCount++;
                }
            } catch (Exception e) {
                log.error("推送通知异常: contactId={}, contactName={}", contact.getId(), contact.getName(), e);
                failCount++;
            }
        }

        log.info("事件通知推送完成: 成功={}, 失败={}, 总计={}", successCount, failCount, contacts.size());
    }

    private Map<String, String> buildMessageData(MyEvent event, String userName, String eventType) {
        Map<String, String> data = new HashMap<>();
        data.put("thing1", userName);
        data.put("thing2", eventType);
        data.put("date3", event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "待定");
        data.put("thing4", event.getLocation() != null ? event.getLocation() : "待定");
        data.put("thing5", event.getTitle() != null ? event.getTitle() : "邀请");
        return data;
    }

    private String getEventTypeText(Integer eventType) {
        if (eventType == null) {
            return "活动";
        }
        switch (eventType) {
            case 1:
                return "婚礼";
            case 2:
                return "生日宴会";
            case 3:
                return "乔迁宴";
            default:
                return "聚会";
        }
    }
}
