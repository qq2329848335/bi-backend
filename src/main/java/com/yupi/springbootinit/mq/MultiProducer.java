package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class MultiProducer {

  private static final String MULTI_QUEUE_NAME = "multi_queue";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //参数queueName, durable, exclusive, autoDelete
            //queueName：消息队列名称（注意，同名的消息队列，只能用同样的参数创建一次）
            //durabale：消息队列持久化，若设置为true，服务重启后队列不丢失：
            //exclusive：是否只允许当前这个创建消息队列的连接操作消息队列
            //autoDelete：没有人用队列后，是否要删除队列
            //map:消息队列参数
            channel.queueDeclare(MULTI_QUEUE_NAME, true, false, false, null);

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                channel.basicPublish("", MULTI_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }

}
