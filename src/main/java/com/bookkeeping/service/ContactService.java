package com.bookkeeping.service;

import com.bookkeeping.vo.req.ContactListReqVO;
import com.bookkeeping.vo.req.CreateContactReqVO;
import com.bookkeeping.vo.req.UpdateContactReqVO;
import com.bookkeeping.vo.resp.ContactRespVO;
import com.bookkeeping.vo.resp.PageResult;

import java.util.List;

public interface ContactService {

    ContactRespVO createContact(Long userId, CreateContactReqVO reqVO);

    ContactRespVO updateContact(Long userId, UpdateContactReqVO reqVO);

    void deleteContact(Long userId, Long id);

    ContactRespVO getContactDetail(Long userId, Long id);

    PageResult<ContactRespVO> getContactList(Long userId, ContactListReqVO reqVO);

    List<ContactRespVO> getContactsByIds(List<Long> ids);

    List<ContactRespVO> getContactsFromRecords(Long userId, List<Long> categoryIds);
}
