package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.service.ShareService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.CreateShareReqVO;
import com.bookkeeping.vo.req.ShareCodeReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.resp.Result;
import com.bookkeeping.vo.resp.ShareRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
@Tag(name = "分享管理", description = "分享邀请相关接口")
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/create")
    @OperLog("创建分享邀请")
    @Operation(summary = "创建分享邀请", description = "创建分享链接（事件邀请或好友邀请）")
    public Result<ShareRespVO> create(@Valid @RequestBody UnifiedRequest<CreateShareReqVO> req) {
        Long userId = UserContext.getUserId();
        ShareRespVO result = shareService.createShare(userId, req.getData());
        return Result.success("创建成功", result);
    }

    @PostMapping("/info")
    @Operation(summary = "获取分享信息", description = "通过分享码获取分享信息（无需登录）")
    public Result<ShareRespVO> info(@Valid @RequestBody UnifiedRequest<ShareCodeReqVO> req) {
        ShareRespVO result = shareService.getShareInfo(req.getData().getShareCode());
        return Result.success(result);
    }

    @PostMapping("/accept")
    @OperLog("接受分享邀请")
    @Operation(summary = "接受分享邀请", description = "接受分享，建立好友关系")
    public Result<Void> accept(@Valid @RequestBody UnifiedRequest<ShareCodeReqVO> req) {
        Long userId = UserContext.getUserId();
        shareService.acceptShare(userId, req.getData().getShareCode());
        return Result.success("接受成功");
    }
}
