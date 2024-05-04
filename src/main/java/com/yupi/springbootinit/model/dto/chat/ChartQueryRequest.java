package com.yupi.springbootinit.model.dto.chat;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * chart图表查询参数
 */
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 8878326647736641880L;

    /**
     * id
     */
    private Long id;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
