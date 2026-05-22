package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> list;

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

    public static <T> PageResult<T> of(List<T> list, Long total, Long current, Long size) {
        Long pages = (total + size - 1) / size;
        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .hasPrevious(current > 1)
                .hasNext(current < pages)
                .build();
    }
}
