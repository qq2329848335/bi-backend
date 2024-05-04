package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.chat.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.ChartVO;

import java.util.List;

/**
 * <p>
 * 图表 服务类
 * </p>
 *
 * @author 加棉
 * @since 2024-04-17
 */
public interface ChartService extends IService<Chart> {

    Page<ChartVO> listChartVOByPage(ChartQueryRequest chartQueryRequest);

    ChartVO getChartVO(Chart chart);

    List<ChartVO> getChartVO(List<Chart> ChartList);

    LambdaQueryWrapper<Chart> getLambdaQueryWrapper(ChartQueryRequest chartQueryRequest);

    Page<Chart> listChartByPage(ChartQueryRequest chartQueryRequest);
}
