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
@Schema(description = "用户响应VO")
public class UserRespVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "角色：0-普通用户，1-管理员")
    private Integer role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
