package com.yupi.springbootinit.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        log.info("MyMessageProducer发送消息,exchange="+BiMqConstant.BI_EXCHANGE_NAME+",routingKey="+BiMqConstant.BI_WORK_QUEUE_BING_ROUTING_KEY +",message="+message);
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_WORK_QUEUE_BING_ROUTING_KEY,message);
    }
}
