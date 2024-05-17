package com.yupi.springbootinit.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@Slf4j
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, String message) {
        log.info("MyMessageProducer发送消息,exchange="+exchange+",routingKey="+routingKey+",message="+message);
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
