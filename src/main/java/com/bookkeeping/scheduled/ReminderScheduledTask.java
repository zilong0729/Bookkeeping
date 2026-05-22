package com.bookkeeping.scheduled;

import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.service.WechatPushService;
import com.bookkeeping.vo.MyEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduledTask {

    private final MyEventService myEventService;
    private final UserMapper userMapper;
    private final WechatPushService wechatPushService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        log.info("开始执行提醒任务");
        try {
            List<MyEventVO> pendingEvents = myEventService.getEventsToPush();
            log.info("找到 {} 个待推送的事件", pendingEvents.size());

            for (MyEventVO event : pendingEvents) {
                try {
                    log.info("处理事件: id={}, title={}", event.getId(), event.getTitle());
                    
                    User user = userMapper.selectById(event.getUserId());
                    if (user == null) {
                        log.warn("用户不存在，跳过推送: userId={}", event.getUserId());
                        continue;
                    }

                    wechatPushService.pushEventNotification(event, user, event.getContacts());
                    
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
}
