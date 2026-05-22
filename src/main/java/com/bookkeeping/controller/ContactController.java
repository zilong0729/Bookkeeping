package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.ContactListReqVO;
import com.bookkeeping.vo.req.CreateContactReqVO;
import com.bookkeeping.vo.req.IdReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.req.UpdateContactReqVO;
import com.bookkeeping.vo.resp.ContactRespVO;
import com.bookkeeping.vo.resp.PageResult;
import com.bookkeeping.vo.resp.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@Tag(name = "联系人管理", description = "联系人相关接口")
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/create")
    @OperLog("创建联系人")
    @Operation(summary = "创建联系人", description = "创建一个新的联系人")
    public Result<ContactRespVO> create(@Valid @RequestBody UnifiedRequest<CreateContactReqVO> req) {
        Long userId = UserContext.getUserId();
        ContactRespVO respVO = contactService.createContact(userId, req.getData());
        return Result.success("创建成功", respVO);
    }

    @PostMapping("/update")
    @OperLog("更新联系人")
    @Operation(summary = "更新联系人", description = "更新指定的联系人")
    public Result<ContactRespVO> update(@Valid @RequestBody UnifiedRequest<UpdateContactReqVO> req) {
        Long userId = UserContext.getUserId();
        ContactRespVO respVO = contactService.updateContact(userId, req.getData());
        return Result.success("更新成功", respVO);
    }

    @PostMapping("/delete")
    @OperLog("删除联系人")
    @Operation(summary = "删除联系人", description = "删除指定的联系人")
    public Result<Void> delete(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        contactService.deleteContact(userId, req.getData().getId());
        return Result.success("删除成功");
    }

    @PostMapping("/detail")
    @Operation(summary = "获取联系人详情", description = "获取指定联系人的详细信息")
    public Result<ContactRespVO> detail(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        ContactRespVO respVO = contactService.getContactDetail(userId, req.getData().getId());
        return Result.success(respVO);
    }

    @PostMapping("/list")
    @Operation(summary = "获取联系人列表", description = "分页查询联系人列表")
    public Result<PageResult<ContactRespVO>> list(@Valid @RequestBody UnifiedRequest<ContactListReqVO> req) {
        Long userId = UserContext.getUserId();
        PageResult<ContactRespVO> result = contactService.getContactList(userId, req.getData());
        return Result.success(result);
    }
}
