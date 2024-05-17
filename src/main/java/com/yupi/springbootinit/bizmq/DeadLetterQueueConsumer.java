package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
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

/**
 * 死信队列消费者
 *
 * 将拒收的消息、队列满了、处理失败的消息  对应的图表状态改为`failed`
 */
@Component
@Slf4j
public class DeadLetterQueueConsumer {

    @Resource
    private ChartService chartService;

    //@SneakyThrows: 这是一个自定义的注解，通常用于Spring方法中，以抑制（或“偷偷地”处理）方法抛出的异常。
    // 当此注解应用于方法时，Spring将捕获方法抛出的任何异常，并将其转换为内部的Spring异常处理机制，而不是向外抛出。
    // 这允许方法的调用者忽略（或延迟处理）异常。

    //使用@RabbitListener注解: 它告诉Spring容器在启动时创建一个消息监听器来监听名为code_queue的队列并手动确认消息。
    //queues: 指定要监听的队列的名称。
    //ackMode = "MANUAL" 表示手动确认消息。默认自动确认。
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_DEAD_LETTER_QUEUE_NAME},ackMode = "MANUAL")
    //String message 是从队列中接收到的消息体。
    //Channel channel 是RabbitMQ的通道对象，它提供了与RabbitMQ通信的方法，包括发送消息、确认消息等。
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 是一个特殊的参数注解，它用于获取当前消息的投递标签（delivery tag）。投递标签是RabbitMQ用来唯一标识一条消息的。在手动确认模式下，你需要使用这个投递标签来确认消息。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);

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
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        //将状态改为failed
        boolean b = chartService.updateById(updateChart);
        if (!b){
            chartService.handleChartUpdateError(chartId, "更新图表状态失败");
        }
        channel.basicAck(deliveryTag, false);
    }
}
