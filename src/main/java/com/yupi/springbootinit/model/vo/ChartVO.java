/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2024/4/22
 * Time: 17:32
 */
package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChartVO {

    /**
     * 图表id
     */
    private Long id;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 状态 wait,running,succeed,failed
     */
    private String status;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
