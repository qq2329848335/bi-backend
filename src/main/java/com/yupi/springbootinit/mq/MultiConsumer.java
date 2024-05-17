package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

  private static final String MULTI_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(MULTI_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            channel.basicQos(1);

            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    try {
                        //处理消息
                        System.out.println(" [x] Received '" + "编号：" + finalI + ":" + message + "'");
                        Thread.sleep(20000);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                    } catch (InterruptedException _ignored) {
                        Thread.currentThread().interrupt();
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                    }
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            //开启消费监听
            channel.basicConsume(MULTI_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
        }
    }
}
