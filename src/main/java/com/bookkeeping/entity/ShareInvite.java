package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("share_invite")
@Schema(description = "分享邀请记录实体")
public class ShareInvite {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "分享者用户ID")
    private Long shareUserId;

    @Schema(description = "分享类型：1-事件邀请，2-好友邀请")
    private Integer shareType;

    @Schema(description = "关联ID（事件ID等）")
    private Long relatedId;

    @Schema(description = "分享码（唯一）")
    private String shareCode;

    @Schema(description = "接受邀请的用户ID")
    private Long acceptUserId;

    @Schema(description = "接受时间")
    private LocalDateTime acceptTime;

    @Schema(description = "状态：0-待接受，1-已接受，2-已过期")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "关系描述")
    private String relation;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;
}
