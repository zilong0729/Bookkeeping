package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.vo.req.ContactListReqVO;
import com.bookkeeping.vo.req.CreateContactReqVO;
import com.bookkeeping.vo.req.UpdateContactReqVO;
import com.bookkeeping.vo.resp.ContactRespVO;
import com.bookkeeping.vo.resp.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMapper contactMapper;
    private final RecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactRespVO createContact(Long userId, CreateContactReqVO reqVO) {
        Contact contact = new Contact();
        BeanUtils.copyProperties(reqVO, contact);
        contact.setUserId(userId);
        contactMapper.insert(contact);
        return convertToRespVO(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactRespVO updateContact(Long userId, UpdateContactReqVO reqVO) {
        Contact contact = contactMapper.selectById(reqVO.getId());
        if (contact == null || contact.getDeleted() == 1) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        BeanUtils.copyProperties(reqVO, contact, "id", "userId", "createTime");
        contactMapper.updateById(contact);
        return convertToRespVO(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long userId, Long id) {
        Contact contact = contactMapper.selectById(id);
        if (contact == null || contact.getDeleted() == 1) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }
        contactMapper.deleteById(id);
    }

    @Override
    public ContactRespVO getContactDetail(Long userId, Long id) {
        Contact contact = contactMapper.selectById(id);
        if (contact == null || contact.getDeleted() == 1 || !contact.getUserId().equals(userId)) {
            throw new BusinessException("联系人不存在");
        }
        return convertToRespVO(contact);
    }

    @Override
    public PageResult<ContactRespVO> getContactList(Long userId, ContactListReqVO reqVO) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getUserId, userId);
        wrapper.eq(Contact::getDeleted, 0);

        String keyword = reqVO != null ? reqVO.getKeyword() : null;
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Contact::getName, keyword)
                    .or().like(Contact::getPhone, keyword)
                    .or().like(Contact::getRelation, keyword));
        }

        wrapper.orderByDesc(Contact::getCreateTime);

        Long current = reqVO != null && reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO != null && reqVO.getSize() != null ? reqVO.getSize() : 10L;

        Page<Contact> page = new Page<>(current, size);
        Page<Contact> contactPage = contactMapper.selectPage(page, wrapper);

        List<ContactRespVO> voList = contactPage.getRecords().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, contactPage.getTotal(), current, size);
    }

    @Override
    public List<ContactRespVO> getContactsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Contact::getId, ids);
        wrapper.eq(Contact::getDeleted, 0);
        List<Contact> contacts = contactMapper.selectList(wrapper);
        return contacts.stream().map(this::convertToRespVO).collect(Collectors.toList());
    }

    @Override
    public List<ContactRespVO> getContactsFromRecords(Long userId, List<Long> categoryIds) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getDeleted, 0);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            wrapper.in(Record::getCategoryId, categoryIds);
        }
        wrapper.isNotNull(Record::getContactName);
        wrapper.groupBy(Record::getContactName);
        wrapper.orderByDesc(Record::getCreateTime);

        List<Record> records = recordMapper.selectList(wrapper);
        List<ContactRespVO> contacts = new ArrayList<>();

        for (Record record : records) {
            if (record.getContactName() == null || record.getContactName().isEmpty()) {
                continue;
            }
            contacts.add(ContactRespVO.builder().name(record.getContactName()).build());
        }
        return contacts;
    }

    private ContactRespVO convertToRespVO(Contact contact) {
        return ContactRespVO.builder()
                .id(contact.getId())
                .name(contact.getName())
                .phone(contact.getPhone())
                .openId(contact.getOpenId())
                .relation(contact.getRelation())
                .remark(contact.getRemark())
                .createTime(contact.getCreateTime() != null ? contact.getCreateTime().toString() : null)
                .build();
    }
}
