package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.ChartConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chat.GenChartByAiRequest;
import com.yupi.springbootinit.model.dto.chat.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.ChartVO;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 * 图表信息表 前端控制器
 * </p>
 *
 * @author 加棉
 * @since 2024-04-17
 */
@Data
@RestController
@RequestMapping("/chart")
@ConfigurationProperties(prefix = "yuapi.client")
public class ChartController {


    private static final Logger log = LoggerFactory.getLogger(ChartController.class);
    private long biModelId;
    @Resource
    UserService userService;
    @Resource
    AiManager aiManager;
    @Resource
    ChartService chartService;
    @Autowired
    RedisLimiterManager redisLimiterManager;
    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    /**
     * 智能分析同步
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<ChartVO> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                              GenChartByAiRequest genChartByAiRequest,
                                              HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        //校验文件
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "未上传文件");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > ChartConstant.ONE_MB, ErrorCode.PARAMS_ERROR, "文件不能超过10MB");
        //校验文件后缀名
        String OriginalFilename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(!ChartConstant.isSupportFileType(OriginalFilename), ErrorCode.PARAMS_ERROR, "文件格式错误");
        //必须登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");

        //限流,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //构造用户输入
        //todo StringBuilder线程不安全/性能好,StringBuffer 线程安全
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接用户目标
        String userGoal = goal + ",请使用" + chartType;
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        //调用AI获得结果
        String result = aiManager.doChat(biModelId, userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }
        //从AI响应中，取出数据
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        //插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库异常，保存图表失败");
        }
        //返回数据给前端
        ChartVO chartVO = chartService.getChartVO(chart);
        return ResultUtils.success(chartVO);
    }


    /**
     * 智能分析异步
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<ChartVO> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                   GenChartByAiRequest genChartByAiRequest,
                                                   HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        //校验文件
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "未上传文件");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > ChartConstant.ONE_MB, ErrorCode.PARAMS_ERROR, "文件不能超过10MB");
        //校验文件后缀名
        String OriginalFilename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(!ChartConstant.isSupportFileType(OriginalFilename), ErrorCode.PARAMS_ERROR, "文件格式错误");
        //必须登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");

        //限流,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //构造用户输入
        //todo StringBuilder线程不安全/性能好,StringBuffer 线程安全
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接用户目标
        String userGoal = goal + ",请使用" + chartType;
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的csv数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //1.先把图表立刻保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus(ChartConstant.CHART_STATUS_WAIT);
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "数据库异常，保存图表失败");

        CompletableFuture.runAsync(() -> {

            //2.调用AI服务
            //先修改图表任务状态为“执行中”。等执行成功后，修改为“已完成”、保存执行结果；
            // 执行失败后，状态修改为“失败”，记录任务失败信息
            Chart updataChart = new Chart();
            updataChart.setId(chart.getId());
            updataChart.setStatus(ChartConstant.CHART_STATUS_RUNNING);
            boolean update = chartService.updateById(updataChart);
            if (!update) {
                handleChartUpdateError(chart.getId(), "数据库异常，更新图表执行中状态失败");
                return;
            }
            String[] splits = null;
            //调用AI获得结果
            String result = aiManager.doChat(biModelId, userInput.toString());
            splits = result.split("【【【【【");
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI生成错误");
                return;
            }
            //从AI响应中，取出数据
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //插入到数据库
            Chart updataChartResult = new Chart();
            updataChartResult.setId(chart.getId());
            updataChartResult.setGenChart(genChart);
            updataChartResult.setGenResult(genResult);
            updataChartResult.setStatus(ChartConstant.CHART_STATUS_SUCCEED);
            boolean b = chartService.updateById(updataChartResult);
            if (!b) {
                handleChartUpdateError(chart.getId(), "数据库异常，更新图表成功状态失败");
                return;
            }
        }, threadPoolExecutor);
        //返回数据给前端
        ChartVO chartVO = new ChartVO();
        chartVO.setId(chart.getId());
        chartVO.setName(name);
        chartVO.setGoal(goal);
        chartVO.setChartType(chartType);
        return ResultUtils.success(chartVO);
    }

    /**
     * 处理更新图表状态失败
     * 时间点 30 12:00
     *
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updataChart = new Chart();
        updataChart.setId(chartId);
        updataChart.setStatus(ChartConstant.CHART_STATUS_FAILED);
        updataChart.setExecMessage(execMessage);
        boolean b = chartService.updateById(updataChart);
        if (!b) {
            log.error("更新图表失败" + chartId + ",执行信息:" + execMessage);
        }
    }

    /**
     * 分页查询图表 （仅管理员）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        Page<Chart> chartPage = chartService.listChartByPage(chartQueryRequest);
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页查询图表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<ChartVO>> listChartVOByPage(ChartQueryRequest chartQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询条件为空");
        //必须登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //设置当前登录用户的id设置到查询参数中
        chartQueryRequest.setUserId(loginUser.getId());
        return ResultUtils.success(chartService.listChartVOByPage(chartQueryRequest));
    }

}
