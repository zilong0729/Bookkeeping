package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.dto.ContactDTO;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.vo.ContactVO;
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
    public ContactVO createContact(Long userId, ContactDTO dto) {
        Contact contact = new Contact();
        BeanUtils.copyProperties(dto, contact);
        contact.setUserId(userId);
        contactMapper.insert(contact);
        return convertToVO(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactVO updateContact(Long userId, Long id, ContactDTO dto) {
        Contact contact = contactMapper.selectById(id);
        if (contact == null || contact.getDeleted() == 1) {
            throw new BusinessException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        BeanUtils.copyProperties(dto, contact);
        contactMapper.updateById(contact);
        return convertToVO(contact);
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
    public ContactVO getContactDetail(Long userId, Long id) {
        Contact contact = contactMapper.selectById(id);
        if (contact == null || contact.getDeleted() == 1 || !contact.getUserId().equals(userId)) {
            throw new BusinessException("联系人不存在");
        }
        return convertToVO(contact);
    }

    @Override
    public Page<ContactVO> getContactList(Long userId, String keyword, Long current, Long size) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getUserId, userId);
        wrapper.eq(Contact::getDeleted, 0);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Contact::getName, keyword)
                    .or().like(Contact::getPhone, keyword)
                    .or().like(Contact::getRelation, keyword));
        }

        wrapper.orderByDesc(Contact::getCreateTime);

        Page<Contact> page = new Page<>(current, size);
        Page<Contact> contactPage = contactMapper.selectPage(page, wrapper);

        Page<ContactVO> resultPage = new Page<>(contactPage.getCurrent(), contactPage.getSize(), contactPage.getTotal());
        List<ContactVO> voList = contactPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public List<ContactVO> getContactsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Contact::getId, ids);
        wrapper.eq(Contact::getDeleted, 0);
        List<Contact> contacts = contactMapper.selectList(wrapper);
        return contacts.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ContactVO> getContactsFromRecords(Long userId, List<Long> categoryIds) {
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
        List<ContactVO> contacts = new ArrayList<>();

        for (Record record : records) {
            if (record.getContactName() == null || record.getContactName().isEmpty()) {
                continue;
            }
            ContactVO vo = new ContactVO();
            vo.setName(record.getContactName());
            contacts.add(vo);
        }
        return contacts;
    }

    private ContactVO convertToVO(Contact contact) {
        ContactVO vo = new ContactVO();
        BeanUtils.copyProperties(contact, vo);
        return vo;
    }
}
