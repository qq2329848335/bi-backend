/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2024/5/10
 * Time: 下午4:31
 */
package com.yupi.springbootinit.bizmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 初始化 交换机和消息队列（程序第一次启动前运行一次）
 */
public class BiInitMain {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //创建交换机
        channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME, "direct");
        //控制单个消费者的处理任务挤压数：
        //每个消费者最多同时处理1个任务
        channel.basicQos(1);

        //指定死信队列参数
        Map<String, Object> workQueueArgs = new HashMap<>();
        //当队列里的消息变成死信消息时，这些消息要绑定到哪个交换机
        workQueueArgs.put("x-dead-letter-exchange", BiMqConstant.BI_DEAD_LETTER_QUEUE_ROUTING_KEY);

        //创建队列
        //参数queueName, durable, exclusive, autoDelete
        //queueName：消息队列名称（注意，同名的消息队列，只能用同样的参数创建一次）
        //durabale：消息队列持久化，若设置为true，服务重启后队列不丢失：
        //exclusive：是否只允许当前这个创建消息队列的连接操作消息队列
        //autoDelete：没有人用队列后，是否要删除队列
        //map:消息队列参数
        channel.queueDeclare(BiMqConstant.BI_WORK_QUEUE_NAME, true, false, false, null);

        //绑定消息队列和交换机
        channel.queueBind(BiMqConstant.BI_WORK_QUEUE_NAME, BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_WORK_QUEUE_BING_ROUTING_KEY);


        //创建死信队列
        channel.queueDeclare(BiMqConstant.BI_DEAD_LETTER_QUEUE_NAME, true, false, false, null);
        channel.queueBind(BiMqConstant.BI_DEAD_LETTER_QUEUE_NAME, BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_DEAD_LETTER_QUEUE_ROUTING_KEY);
    }
}
