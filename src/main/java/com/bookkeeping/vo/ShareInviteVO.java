package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "分享邀请VO")
public class ShareInviteVO {

    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "分享码")
    private String shareCode;

    @Schema(description = "分享类型：1-事件邀请，2-好友邀请")
    private Integer shareType;

    @Schema(description = "分享链接")
    private String shareUrl;

    @Schema(description = "关联事件信息（如果是事件邀请）")
    private MyEventVO eventInfo;

    @Schema(description = "分享者用户信息")
    private UserVO shareUser;

    @Schema(description = "状态：0-待接受，1-已接受，2-已过期")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
