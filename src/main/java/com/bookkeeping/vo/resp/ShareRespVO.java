package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分享响应VO")
public class ShareRespVO {

    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "分享码")
    private String shareCode;

    @Schema(description = "分享链接")
    private String shareUrl;

    @Schema(description = "分享类型：1-事件分享、2-账单分享、3-邀请注册")
    private Integer shareType;

    @Schema(description = "状态：0-未使用、1-已使用、2-已过期")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "分享者昵称")
    private String shareNickname;

    @Schema(description = "分享者头像")
    private String shareAvatar;

    @Schema(description = "关联事件")
    private EventRespVO event;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
