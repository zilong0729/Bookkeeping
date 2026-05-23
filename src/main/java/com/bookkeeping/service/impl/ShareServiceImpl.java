package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.FriendRelation;
import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.ShareInvite;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.FriendRelationMapper;
import com.bookkeeping.mapper.MyEventMapper;
import com.bookkeeping.mapper.ShareInviteMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.service.ShareService;
import com.bookkeeping.utils.DistributedLockUtil;
import com.bookkeeping.vo.req.CreateShareReqVO;
import com.bookkeeping.vo.req.ShareCodeReqVO;
import com.bookkeeping.vo.resp.EventRespVO;
import com.bookkeeping.vo.resp.ShareRespVO;
import com.bookkeeping.vo.resp.UserRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 分享服务实现 - 增强并发安全
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareInviteMapper shareInviteMapper;
    private final FriendRelationMapper friendRelationMapper;
    private final ContactMapper contactMapper;
    private final UserMapper userMapper;
    private final MyEventMapper myEventMapper;
    private final DistributedLockUtil lockUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareRespVO createShare(Long userId, CreateShareReqVO reqVO) {
        String shareCode = generateShareCode();

        ShareInvite shareInvite = new ShareInvite();
        shareInvite.setShareUserId(userId);
        shareInvite.setShareType(reqVO.getShareType());
        shareInvite.setRelatedId(reqVO.getRelatedId());
        shareInvite.setRelation(reqVO.getRelation());
        shareInvite.setShareCode(shareCode);
        shareInvite.setStatus(0);
        shareInvite.setExpireTime(LocalDateTime.now().plusDays(7));
        shareInviteMapper.insert(shareInvite);

        return convertToRespVO(shareInvite);
    }

    @Override
    public ShareRespVO getShareInfo(String shareCode) {
        LambdaQueryWrapper<ShareInvite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareInvite::getShareCode, shareCode);
        wrapper.eq(ShareInvite::getDeleted, 0);

        ShareInvite shareInvite = shareInviteMapper.selectOne(wrapper);
        if (shareInvite == null) {
            throw new BusinessException("分享链接不存在或已过期");
        }

        if (LocalDateTime.now().isAfter(shareInvite.getExpireTime())) {
            throw new BusinessException("分享链接已过期");
        }

        return convertToRespVO(shareInvite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptShare(Long userId, String shareCode) {
        String lockKey = "accept_share:" + shareCode + ":" + userId;

        boolean lockAcquired = false;
        try {
            lockAcquired = lockUtil.tryLockWithWait(lockKey, 5);
            if (!lockAcquired) {
                throw new BusinessException("操作进行中，请稍后重试");
            }

            LambdaQueryWrapper<ShareInvite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShareInvite::getShareCode, shareCode);
            wrapper.eq(ShareInvite::getDeleted, 0);

            ShareInvite shareInvite = shareInviteMapper.selectOne(wrapper);
            if (shareInvite == null) {
                throw new BusinessException("分享链接不存在");
            }

            if (LocalDateTime.now().isAfter(shareInvite.getExpireTime())) {
                shareInvite.setStatus(2);
                shareInviteMapper.updateById(shareInvite);
                throw new BusinessException("分享链接已过期");
            }

            if (shareInvite.getStatus() != 0) {
                throw new BusinessException("分享链接已被接受或已过期");
            }

            if (shareInvite.getShareUserId().equals(userId)) {
                throw new BusinessException("不能接受自己的分享");
            }

            shareInvite.setAcceptUserId(userId);
            shareInvite.setAcceptTime(LocalDateTime.now());
            shareInvite.setStatus(1);
            shareInviteMapper.updateById(shareInvite);

            createFriendRelation(shareInvite.getShareUserId(), userId);
            createFriendRelation(userId, shareInvite.getShareUserId());

        } finally {
            if (lockAcquired) {
                lockUtil.unlock(lockKey);
            }
        }
    }

    private void createFriendRelation(Long userId, Long friendUserId) {
        String lockKey = "create_friend:" + userId + ":" + friendUserId;
        boolean lockAcquired = false;

        try {
            lockAcquired = lockUtil.tryLock(lockKey, 10);
            if (!lockAcquired) {
                log.warn("获取好友关系锁失败: userId={}, friendUserId={}", userId, friendUserId);
                return;
            }

            LambdaQueryWrapper<FriendRelation> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(FriendRelation::getUserId, userId);
            checkWrapper.eq(FriendRelation::getFriendUserId, friendUserId);
            checkWrapper.eq(FriendRelation::getDeleted, 0);

            FriendRelation existing = friendRelationMapper.selectOne(checkWrapper);
            if (existing != null) {
                log.debug("好友关系已存在: userId={}, friendUserId={}", userId, friendUserId);
                return;
            }

            User friendUser = userMapper.selectById(friendUserId);
            if (friendUser == null) {
                log.error("好友用户不存在: friendUserId={}", friendUserId);
                return;
            }

            LambdaQueryWrapper<Contact> contactWrapper = new LambdaQueryWrapper<>();
            contactWrapper.eq(Contact::getUserId, userId);
            contactWrapper.eq(Contact::getOpenId, friendUser.getOpenid());
            contactWrapper.eq(Contact::getDeleted, 0);
            Contact existingContact = contactMapper.selectOne(contactWrapper);

            Long contactId;
            if (existingContact != null) {
                contactId = existingContact.getId();
                log.debug("联系人已存在: userId={}, contactId={}", userId, contactId);
            } else {
                Contact contact = new Contact();
                contact.setUserId(userId);
                contact.setName(friendUser.getNickname() != null ? friendUser.getNickname() : "好友");
                contact.setOpenId(friendUser.getOpenid());
                contact.setRelation("朋友");
                contactMapper.insert(contact);
                contactId = contact.getId();
                log.debug("创建新联系人: userId={}, contactId={}", userId, contactId);
            }

            FriendRelation relation = new FriendRelation();
            relation.setUserId(userId);
            relation.setFriendUserId(friendUserId);
            relation.setContactId(contactId);
            relation.setRelation("朋友");
            friendRelationMapper.insert(relation);

            log.info("创建好友关系成功: userId={}, friendUserId={}", userId, friendUserId);

        } catch (Exception e) {
            log.error("创建好友关系失败: userId={}, friendUserId={}", userId, friendUserId, e);
            throw e;
        } finally {
            if (lockAcquired) {
                lockUtil.unlock(lockKey);
            }
        }
    }

    private String generateShareCode() {
        String shareCode;
        int attempts = 0;
        do {
            shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            attempts++;
            if (attempts > 10) {
                throw new BusinessException("生成分享码失败，请重试");
            }
        } while (isShareCodeExists(shareCode));
        return shareCode;
    }

    private boolean isShareCodeExists(String shareCode) {
        LambdaQueryWrapper<ShareInvite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareInvite::getShareCode, shareCode);
        wrapper.eq(ShareInvite::getDeleted, 0);
        return shareInviteMapper.selectCount(wrapper) > 0;
    }

    private ShareRespVO convertToRespVO(ShareInvite shareInvite) {
        ShareRespVO respVO = ShareRespVO.builder()
                .id(shareInvite.getId())
                .shareCode(shareInvite.getShareCode())
                .shareType(shareInvite.getShareType())
                .status(shareInvite.getStatus())
                .expireTime(shareInvite.getExpireTime())
                .createTime(shareInvite.getCreateTime())
                .build();

        String shareUrl = "/pages/index/index?shareCode=" + shareInvite.getShareCode();
        respVO.setShareUrl(shareUrl);

        User shareUser = userMapper.selectById(shareInvite.getShareUserId());
        if (shareUser != null) {
            respVO.setShareNickname(shareUser.getNickname());
            respVO.setShareAvatar(shareUser.getAvatarUrl());
        }

        if (shareInvite.getShareType() == 1 && shareInvite.getRelatedId() != null) {
            MyEvent event = myEventMapper.selectById(shareInvite.getRelatedId());
            if (event != null) {
                EventRespVO eventVO = EventRespVO.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .eventType(event.getEventType())
                        .eventDate(event.getEventDate())
                        .location(event.getLocation())
                        .build();
                respVO.setEvent(eventVO);
            }
        }

        return respVO;
    }
}
