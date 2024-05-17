package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

/**
 * https://www.rabbitmq.com/tutorials/tutorial-six-java
 * RPC服务器代码相当简单：
 * 1.像往常一样，我们首先建立连接、通道并声明队列。
 * 2.我们可能想要运行多个服务器进程。为了将负载均匀地分布在多个服务器上，我们需要 prefetchCount在channel.basicQos中进行设置。
 * 3.我们使用basicConsume来访问队列，在队列中我们以对象 ( ) 的形式提供回调，DeliverCallback该回调将完成工作并将响应发回。
 */
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    // 斐波那契函数
    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {
        //创建工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        //创建连接
        Connection connection = factory.newConnection();
        //创建通道
        Channel channel = connection.createChannel();
        //创建队列
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        //清空队列
        channel.queuePurge(RPC_QUEUE_NAME);

        //确保每个消费者只能同时消费一个消息
        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 创建一个用于响应的BasicProperties对象
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                // 将接收到的消息字节转换为字符串
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);

                // 打印接收到的消息
                System.out.println(" [.] fib(" + message + ")");

                // 计算斐波那契数
                response += fib(n);
            } catch (RuntimeException e) {
                // 如果解析消息或计算斐波那契数时出现异常，打印异常信息
                System.out.println(" [.] " + e);
            } finally {
                // 发送响应消息到replyTo队列，使用replyProps作为消息属性
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));

                // 确认消息已处理，false表示不等待其他消息
                //参数的意义是：如果为true，则所有未确认的消息都将被确认，包括当前消息。如果为false，则只确认当前消息。
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {}));
    }
}
