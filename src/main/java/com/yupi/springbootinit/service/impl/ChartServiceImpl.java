package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.yupi.springbootinit.exception.BusinessException;
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
import com.yupi.springbootinit.model.vo.ChartVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        //根据排序方式排序
        String sortOrder = chartQueryRequest.getSortOrder();
        if (StringUtils.isNotBlank(sortOrder)){
            //是否有排序字段
            String sortField = chartQueryRequest.getSortField();
            if (StringUtils.isNotBlank(sortField)){
                //获取与实体类字段名对应的 SFunction 对象。
                SFunction sFunction = getSFunction(Chart.class, sortField);

                //判断排序方式
                if (sortOrder.equals("asc")){
                    chartLambdaQueryWrapper.orderByAsc(sFunction);
                }else if (sortOrder.equals("desc")){
                    chartLambdaQueryWrapper.orderByDesc(sFunction);
                }else {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"排序方式错误");
                }
            }

        }


        return chartLambdaQueryWrapper;
    }


    // 定义一个标志常量，用于序列化。在 LambdaMetafactory 中使用，
    // 以指示生成的 lambda 表达式应该是可序列化的。
    private static final int FLAG_SERIALIZABLE = 1;

    // 使用 HashMap 缓存 SFunction 对象，以避免重复创建。
    private static Map<String, SFunction> functionMap = new HashMap<>();

    /**
     * 获取与实体类字段对应的 SFunction 对象。
     * @param entityClass 实体类的 Class 对象。
     * @param fieldName 实体类中的字段名。
     * @return 返回找到的 SFunction 对象。
     */
    public static SFunction getSFunction(Class<?> entityClass, String fieldName) {
        // 检查缓存中是否已经有了对应的 SFunction 对象。
        if (functionMap.containsKey(entityClass.getName() + fieldName)) {
            return functionMap.get(entityClass.getName() + fieldName);
        }
        // 获取实体类中名为 fieldName 的字段。
        Field field = getDeclaredField(entityClass, fieldName);
        if (field == null) {
            //如果字段不存在，使用 ExceptionUtils 抛出一个异常，指出实体类中没有找到该字段。
            throw ExceptionUtils.mpe("This class %s is not have field %s ", entityClass.getName(), fieldName);
        }
        SFunction func = null;
        // 获取 MethodHandles.Lookup 实例，用于反射操作。
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        // 定义方法类型，表示实体类的实例方法，该方法返回字段的类型。
        MethodType methodType = MethodType.methodType(field.getType(), entityClass);
        // 用于存储 LambdaMetafactory 创建的 CallSite 对象。
        final CallSite site;
        // 构造标准的 Java getter 方法名。
        String getFunName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            // 使用 LambdaMetafactory 创建一个动态的 SFunction 实例。
            site = LambdaMetafactory.altMetafactory(
                    lookup,
                    "invoke",
                    MethodType.methodType(SFunction.class),
                    methodType,
                    lookup.findVirtual(entityClass, getFunName, MethodType.methodType(field.getType())),
                    methodType,
                    FLAG_SERIALIZABLE
            );
            // 使用 CallSite 来获取 SFunction 实例。
            func = (SFunction) site.getTarget().invokeExact();
            // 将生成的 SFunction 实例存储到缓存中。
            functionMap.put(entityClass.getName() + field.getName(), func);
            return func;
        } catch (Throwable e) {
            // 如果在创建 SFunction 过程中发生异常，抛出异常，指出实体类中没有找到对应的 getter 方法。
            throw ExceptionUtils.mpe("This class %s is not have method %s ", entityClass.getName(), getFunName);
        }
    }

    /**
     * 递归获取类中声明的字段，包括私有字段。
     * @param clazz 要检查的类。
     * @param fieldName 要查找的字段名。
     * @return 返回找到的 Field 对象，如果没有找到则返回 null。
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        Field field = null;
        // 遍历类及其父类，直到到达 Object 类。
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                // 尝试获取声明的字段。
                field = clazz.getDeclaredField(fieldName);
                // 如果找到字段，返回该字段。
                return field;
            } catch (NoSuchFieldException e) {
                // 如果没有找到字段，继续查找父类。
                // 这里不处理异常，让其继续执行循环。
            }
        }
        // 如果没有找到字段，返回 null。
        return null;
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

