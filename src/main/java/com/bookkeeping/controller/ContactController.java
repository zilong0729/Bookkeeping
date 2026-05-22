package com.bookkeeping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.ContactDTO;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.ContactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping
    @OperLog("创建联系人")
    @Operation(summary = "创建联系人", description = "创建一个新的联系人")
    public Result<ContactVO> createContact(@Valid @RequestBody ContactDTO dto) {
        Long userId = UserContext.getUserId();
        ContactVO vo = contactService.createContact(userId, dto);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @OperLog("更新联系人")
    @Operation(summary = "更新联系人", description = "更新指定的联系人")
    public Result<ContactVO> updateContact(
            @Parameter(description = "联系人ID") @PathVariable("id") Long id,
            @Valid @RequestBody ContactDTO dto) {
        Long userId = UserContext.getUserId();
        ContactVO vo = contactService.updateContact(userId, id, dto);
        return Result.success("更新成功", vo);
    }

    @DeleteMapping("/{id}")
    @OperLog("删除联系人")
    @Operation(summary = "删除联系人", description = "删除指定的联系人")
    public Result<Void> deleteContact(
            @Parameter(description = "联系人ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        contactService.deleteContact(userId, id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取联系人详情", description = "获取指定联系人的详细信息")
    public Result<ContactVO> getContactDetail(
            @Parameter(description = "联系人ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        ContactVO vo = contactService.getContactDetail(userId, id);
        return Result.success(vo);
    }

    @GetMapping("/list")
    @Operation(summary = "获取联系人列表", description = "分页查询联系人列表")
    public Result<PageResult<ContactVO>> getContactList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        Page<ContactVO> page = contactService.getContactList(userId, keyword, current, size);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/from-records")
    @Operation(summary = "从账单获取联系人", description = "从历史账单中提取联系人")
    public Result<List<ContactVO>> getContactsFromRecords(
            @Parameter(description = "账单类别ID列表") @RequestParam(required = false) List<Long> categoryIds) {
        Long userId = UserContext.getUserId();
        List<ContactVO> contacts = contactService.getContactsFromRecords(userId, categoryIds);
        return Result.success(contacts);
    }
}
