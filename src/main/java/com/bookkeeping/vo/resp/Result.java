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
@Schema(description = "统一返回体")
public class Result<T> {

    @Schema(description = "响应状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "分页信息")
    private PageInfo page;

    @Schema(description = "响应时间")
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页信息")
    public static class PageInfo {
        @Schema(description = "总记录数")
        private Long total;

        @Schema(description = "每页条数")
        private Long size;

        @Schema(description = "当前页码")
        private Long current;

        @Schema(description = "总页数")
        private Long pages;

        @Schema(description = "是否有上一页")
        private Boolean hasPrevious;

        @Schema(description = "是否有下一页")
        private Boolean hasNext;
    }

    public static <T> Result<T> success(String message) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> Result<T> success(T data, PageInfo page) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .page(page)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
