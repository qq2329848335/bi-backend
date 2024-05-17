package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DelayedQueueConsumer {
    // 定义队列名称常量
    private static final String QUEUE_NAME = "delayed_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // 设置RabbitMQ服务器地址

        // 创建一个新的连接和通道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // 设置消费者，这里使用匿名内部类的方式
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received message: " + message);

                // 确认消息，告知RabbitMQ此消息已被成功处理
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };


            // 监听队列，自动确认消息
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

            // 确保消费者在JVM退出时能够正常关闭
            new Thread(() -> {
                try {
                    System.in.read(); // 按任意键退出程序
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
