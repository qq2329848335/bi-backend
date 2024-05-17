package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyMessageConsumer {

    //@SneakyThrows: 这是一个自定义的注解，通常用于Spring方法中，以抑制（或“偷偷地”处理）方法抛出的异常。
    // 当此注解应用于方法时，Spring将捕获方法抛出的任何异常，并将其转换为内部的Spring异常处理机制，而不是向外抛出。
    // 这允许方法的调用者忽略（或延迟处理）异常。

    //使用@RabbitListener注解: 它告诉Spring容器在启动时创建一个消息监听器来监听名为code_queue的队列并手动确认消息。
    //queues: 指定要监听的队列的名称。
    //ackMode = "MANUAL" 表示手动确认消息。默认自动确认。
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    //String message 是从队列中接收到的消息体。
    //Channel channel 是RabbitMQ的通道对象，它提供了与RabbitMQ通信的方法，包括发送消息、确认消息等。
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 是一个特殊的参数注解，它用于获取当前消息的投递标签（delivery tag）。投递标签是RabbitMQ用来唯一标识一条消息的。在手动确认模式下，你需要使用这个投递标签来确认消息。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        channel.basicAck(deliveryTag, false);
    }
}
