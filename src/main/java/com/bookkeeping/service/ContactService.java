package com.bookkeeping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.vo.ContactVO;

import java.util.List;

public interface ContactService {
    ContactVO createContact(Long userId, com.bookkeeping.dto.ContactDTO dto);

    ContactVO updateContact(Long userId, Long id, com.bookkeeping.dto.ContactDTO dto);

    void deleteContact(Long userId, Long id);

    ContactVO getContactDetail(Long userId, Long id);

    Page<ContactVO> getContactList(Long userId, String keyword, Long current, Long size);

    List<ContactVO> getContactsByIds(List<Long> ids);

    List<ContactVO> getContactsFromRecords(Long userId, List<Long> categoryIds);
}
