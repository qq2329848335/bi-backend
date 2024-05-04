package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.ChartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.chat.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.vo.ChartVO;
import com.yupi.springbootinit.service.ChartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 图表信息表 服务实现类
 * </p>
 *
 * @author 加棉
 * @since 2024-04-17
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Override
    public Page<ChartVO> listChartVOByPage(ChartQueryRequest chartQueryRequest) {
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR,"查询条件为空");
        int currentPage = chartQueryRequest.getCurrentPage();
        int pageSize = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = this.page(new Page<>(currentPage, pageSize),
                this.getLambdaQueryWrapper(chartQueryRequest));
        Page<ChartVO> chartVOPage = new Page<>(currentPage, pageSize, chartPage.getTotal());
        List<ChartVO> chartVOList = this.getChartVO(chartPage.getRecords());
        chartVOPage.setRecords(chartVOList);
        return chartVOPage;
    }


    /**
     * 将Chart对象转换为ChartVO对象
     * @param chart
     * @return
     */
    @Override
    public ChartVO getChartVO(Chart chart) {
        if (chart == null) {
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }

    /**
     * 将Chart列表转换为ChartVO列表
     * @param ChartList
     * @return
     */
    @Override
    public List<ChartVO> getChartVO(List<Chart> ChartList) {
        if (CollUtil.isEmpty(ChartList)) {
            return new ArrayList<>();
        }
        //使用Stream API对ChartList进行映射，调用getChartVO(Chart Chart)方法将每个Chart对象转换为ChartVO对象，
        //并将转换后的结果收集到一个新的列表中返回。
        return ChartList.stream().map(this::getChartVO).collect(Collectors.toList());
    }


    /**
     * 封装查询条件
     * @param chartQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Chart> getLambdaQueryWrapper(ChartQueryRequest chartQueryRequest){
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR,"查询条件为空");
        LambdaQueryWrapper<Chart> chartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据id查询
        Long id = chartQueryRequest.getId();
        if (id != null) {
            chartLambdaQueryWrapper.eq(Chart::getId, id);
        }
        //根据name查询
        String name = chartQueryRequest.getName();
        if (StringUtils.isNotBlank(name)) {
            name=name.trim();
            chartLambdaQueryWrapper.like(Chart::getName, name);
        }
        //根据chartType图表类型查询
        String chartType = chartQueryRequest.getChartType();
        if (chartType != null) {
            chartLambdaQueryWrapper.eq(Chart::getChartType, chartType);
        }
        //根据userId查询
        Long userId = chartQueryRequest.getUserId();
        if (userId != null) {
            chartLambdaQueryWrapper.eq(Chart::getUserId, userId);
        }
        //根据createTime查询
        LocalDateTime createTime = chartQueryRequest.getCreateTime();
        if (createTime != null) {
            chartLambdaQueryWrapper.le(Chart::getCreateTime, createTime);
        }
        //根据updateTime查询
        LocalDateTime updateTime = chartQueryRequest.getUpdateTime();
        if (updateTime != null) {
            chartLambdaQueryWrapper.le(Chart::getUpdateTime, updateTime);
        }
        return chartLambdaQueryWrapper;
    }

    @Override
    public Page<Chart> listChartByPage(ChartQueryRequest chartQueryRequest) {
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR,"查询条件为空");
        long current = chartQueryRequest.getCurrentPage();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> ChartPage = this.page(new Page<>(current, size),
                this.getLambdaQueryWrapper(chartQueryRequest));
        return ChartPage;
    }
}

