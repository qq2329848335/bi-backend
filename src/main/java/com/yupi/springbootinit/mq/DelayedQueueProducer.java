package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*; // 导入RabbitMQ客户端库

import java.io.IOException; // 导入可能发生的IO异常类
import java.util.HashMap; // 导入HashMap类
import java.util.Map; // 导入Map接口
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ延迟队列的基本示例
 * 这需要安装相应的RabbitMQ插件--rabbitmq_delayed_message_exchange
 */
public class DelayedQueueProducer {
    // 定义交换机、队列和路由键的名称常量
    private static final String EXCHANGE_NAME = "delayed_exchange";
    private static final String QUEUE_NAME = "delayed_queue";
    private static final String ROUTING_KEY = "delayed_routing_key";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接工厂并设置连接参数
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // 设置RabbitMQ服务器地址为本地

        // 通过连接工厂创建一个新的连接
        Connection connection = factory.newConnection();

        // 创建一个新的通道（Channel）
        Channel channel = connection.createChannel();

        //exchangeDeclare()方法中各个参数的意义:
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
        // 创建一个支持延时队列的Exchange（交换机）
        Map<String, Object> arguments = new HashMap<>();
        // 设置交换机类型为"x-delayed-message"，这需要安装相应的RabbitMQ插件
        arguments.put("x-delayed-type", "direct");

        // 声明交换机，持久化，不自动删除，使用上面创建的参数
        //参数含义：
        //1. exchangeName: 交换机的名称。
        //2. type: `"x-delayed-message"` 是交换机的类型。这是一个特殊的交换机类型，用于支持延迟消息。它需要 `rabbitmq_delayed_message_exchange` 插件支持，这个插件不是RabbitMQ的内置部分，需要单独安装。
        //3. durable: true表示这个交换机是持久的。持久的交换机在RabbitMQ服务器重启后依然存在，非持久的交换机则会丢失。
        //4. autoDelete: false`表示不会在没有绑定的队列或消费者时自动删除这个交换机。如果设置为 `true`，一旦最后一个队列或消费者解绑了该交换机，交换机将被自动删除。
        //5. arguments: arguments 是一个 Map<String, Object> 类型的参数，用于为交换机设置额外的属性。在你提供的代码中，`arguments` 被设置为一个新创建的 `HashMap`，并且向其中添加了一个键值对：
        //- `"x-delayed-type"`: 这个键用于指定延迟交换机的基础交换机类型。值 `"direct"` 表示底层的交换机类型是 `direct`。这意味着延迟交换机将按照 `direct` 交换机的路由逻辑来处理消息。
        //通过这个 `exchangeDeclare` 调用，你声明了一个名为 `"delayed_exchange"` 的延迟交换机，它是持久的，不会自动删除，并且基于 `direct` 交换机的路由逻辑。
        channel.exchangeDeclare(EXCHANGE_NAME, "x-delayed-message", true, false, arguments);

        // 创建一个延时队列
        Map<String, Object> queueArguments = new HashMap<>();
        // 设置死信交换机（当消息过期后转发到此交换机）
        queueArguments.put("x-dead-letter-exchange", "");
        // 设置死信路由键
        queueArguments.put("x-dead-letter-routing-key", QUEUE_NAME);
        // 设置消息的TTL（Time-To-Live，存活时间），单位为毫秒
        queueArguments.put("x-message-ttl", 5000);
        // 声明队列，持久化，不排他，不自动删除，使用上面创建的参数
        channel.queueDeclare(QUEUE_NAME, true, false, false, queueArguments);

        // 将队列绑定到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        // 发送消息到延时队列中
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                // 设置消息的过期时间，当此时间过后消息会成为死信
                .expiration("10000") // 设置消息的过期时间为10000毫秒
                .build();
        // 定义要发送的消息
        String message = "Hello, delayed queue!";
        // 将消息发送到指定的交换机和路由键
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, properties, message.getBytes());
        // 打印消息确认
        System.out.println("Sent message to delayed queue: " + message);

        // 关闭通道和连接
        channel.close();
        connection.close();
    }
}
