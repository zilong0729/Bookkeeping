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

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareInviteMapper shareInviteMapper;
    private final FriendRelationMapper friendRelationMapper;
    private final ContactMapper contactMapper;
    private final UserMapper userMapper;
    private final MyEventMapper myEventMapper;

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
    }

    private void createFriendRelation(Long userId, Long friendUserId) {
        LambdaQueryWrapper<FriendRelation> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FriendRelation::getUserId, userId);
        checkWrapper.eq(FriendRelation::getFriendUserId, friendUserId);
        checkWrapper.eq(FriendRelation::getDeleted, 0);

        FriendRelation existing = friendRelationMapper.selectOne(checkWrapper);
        if (existing != null) {
            return;
        }

        User friendUser = userMapper.selectById(friendUserId);

        Contact contact = new Contact();
        contact.setUserId(userId);
        contact.setName(friendUser.getNickname() != null ? friendUser.getNickname() : "好友");
        contact.setOpenId(friendUser.getOpenid());
        contact.setRelation("朋友");
        contactMapper.insert(contact);

        FriendRelation relation = new FriendRelation();
        relation.setUserId(userId);
        relation.setFriendUserId(friendUserId);
        relation.setContactId(contact.getId());
        relation.setRelation("朋友");
        friendRelationMapper.insert(relation);
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
