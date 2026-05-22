package com.bookkeeping.scheduled;

import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.service.WechatPushService;
import com.bookkeeping.vo.ContactVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduledTask {

    private final MyEventService myEventService;
    private final UserMapper userMapper;
    private final WechatPushService wechatPushService;
    private final ContactMapper contactMapper;

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        log.info("开始执行提醒任务");
        try {
            List<MyEvent> pendingEvents = myEventService.getEventsToPush();
            log.info("找到 {} 个待推送的事件", pendingEvents.size());

            for (MyEvent event : pendingEvents) {
                try {
                    log.info("处理事件: id={}, title={}", event.getId(), event.getTitle());
                    
                    User user = userMapper.selectById(event.getUserId());
                    if (user == null) {
                        log.warn("用户不存在，跳过推送: userId={}", event.getUserId());
                        continue;
                    }

                    List<ContactVO> contacts = getContactsForUser(event.getUserId());
                    wechatPushService.pushEventNotification(event, user, contacts);
                    
                    myEventService.markAsPushed(event.getId());
                } catch (Exception e) {
                    log.error("处理事件失败: id={}", event.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("执行提醒任务失败", e);
        }
        log.info("提醒任务执行完成");
    }

    private List<ContactVO> getContactsForUser(Long userId) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getUserId, userId);
        wrapper.eq(Contact::getDeleted, 0);
        
        List<Contact> contacts = contactMapper.selectList(wrapper);
        
        return contacts.stream().map(contact -> {
            ContactVO vo = new ContactVO();
            BeanUtils.copyProperties(contact, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
