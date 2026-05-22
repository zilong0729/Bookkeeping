package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friend_relation")
@Schema(description = "好友关系实体")
public class FriendRelation {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "关系ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "好友用户ID")
    private Long friendUserId;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "关系描述")
    private String relation;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;
}
