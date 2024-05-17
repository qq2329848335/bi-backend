package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.Constant;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.ChartConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;


    //@SneakyThrows: 这是一个自定义的注解，通常用于Spring方法中，以抑制（或“偷偷地”处理）方法抛出的异常。
    // 当此注解应用于方法时，Spring将捕获方法抛出的任何异常，并将其转换为内部的Spring异常处理机制，而不是向外抛出。
    // 这允许方法的调用者忽略（或延迟处理）异常。

    //使用@RabbitListener注解: 它告诉Spring容器在启动时创建一个消息监听器来监听名为code_queue的队列并手动确认消息。
    //queues: 指定要监听的队列的名称。
    //ackMode = "MANUAL" 表示手动确认消息。默认自动确认。
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_WORK_QUEUE_NAME},ackMode = "MANUAL")
    //String message 是从队列中接收到的消息体。
    //Channel channel 是RabbitMQ的通道对象，它提供了与RabbitMQ通信的方法，包括发送消息、确认消息等。
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 是一个特殊的参数注解，它用于获取当前消息的投递标签（delivery tag）。投递标签是RabbitMQ用来唯一标识一条消息的。在手动确认模式下，你需要使用这个投递标签来确认消息。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isBlank(message)){
            //拒绝消息
            //各个参数的含义：
            //deliveryTag: 这是消息的投递标签，是一个长整型（long）值，用于唯一标识一个消息。当你从队列中接收到消息时，RabbitMQ 	  会为每条消息分配一个递增的投递标签。
            //multiple: 这是一个布尔值，用于指定是否对消息进行单个否定确认或多个否定确认。如果设置为 true，则否定确认当前消息以及		  所有小于当前投递标签的消息。如果设置为 false（默认值），则只对单个消息进行否定确认。
            //requeue: 这也是一个布尔值，用于指定是否将消息重新入队。如果设置为 true，则消息将被放回队列的末尾，以便它可以被另一个		 消费者重新消费。如果设置为 false，则消息将不会被重新入队，而是根据RabbitMQ的死信队列设置进行处理。
            channel.basicNack(deliveryTag, false, false);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null){
            //拒绝消息,并不返回队列中
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }


        //2.调用AI服务
        //先修改图表任务状态为“执行中”。等执行成功后，修改为“已完成”、保存执行结果；
        // 执行失败后，状态修改为“失败”，记录任务失败信息
        Chart updataChart = new Chart();
        updataChart.setId(chartId);
        updataChart.setStatus(ChartConstant.CHART_STATUS_RUNNING);
        boolean update = chartService.updateById(updataChart);
        if (!update) {
            chartService.handleChartUpdateError(chartId, "数据库异常，更新图表执行中状态失败");
            return;
        }
        String[] splits = null;
        //调用AI获得结果
        String result = null;
        try {
            result = aiManager.doChat(Constant.BI_MODEL_ID, buildUserInput(chart));
        } catch (Exception e) {
            //更新状态为失败
            chartService.handleChartUpdateError(chartId, "AI 服务报错,具体报错消息:"+e.getMessage());
            //把异常抛出去,因为这是致命的异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务报错,具体报错信息:"+e.getMessage());
        }
        splits = result.split("【【【【【");
        if (splits.length < 3) {
            chartService.handleChartUpdateError(chartId, "AI生成格式错误");
            return;
        }
        //从AI响应中，取出数据
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        //插入到数据库
        Chart updataChartResult = new Chart();
        updataChartResult.setId(chartId);
        updataChartResult.setGenChart(genChart);
        updataChartResult.setGenResult(genResult);
        updataChartResult.setStatus(ChartConstant.CHART_STATUS_SUCCEED);
        updataChartResult.setExecMessage("");
        boolean b = chartService.updateById(updataChartResult);
        if (!b) {
            chartService.handleChartUpdateError(chartId, "数据库异常，更新图表成功状态失败");
            return;
        }
        channel.basicAck(deliveryTag, false);
    }


    //构建输入input
    public String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接用户目标
        String userGoal = goal + ",请使用" + chartType;
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //图表原始数据
        userInput.append(chart.getChartData()).append("\n");
        return userInput.toString();


    }
}
