package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单记录实体类
 */
@Data
@TableName("record")
public class Record {

    /**
     * 账单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类别ID
     */
    private Long categoryId;

    /**
     * 类型：1-收入，2-支出
     */
    private Integer type;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 往来对象姓名
     */
    private String contactName;

    /**
     * 关联联系人ID
     */
    private Long contactId;

    /**
     * 账单日期
     */
    private LocalDate recordDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
