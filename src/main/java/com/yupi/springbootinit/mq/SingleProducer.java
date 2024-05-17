package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class SingleProducer {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        //创建到服务器的连接：
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //声明一个队列供我们发送,声明队列是幂等的 - 仅当队列尚不存在时才会创建它。
            //参数queueName, durable, exclusive, autoDelete
            //queueName：消息队列名称（注意，同名的消息队列，只能用同样的参数创建一次）
            //durabale：消息队列持久化，若设置为true，服务重启后队列不丢失：
            //exclusive：是否只允许当前这个创建消息队列的连接操作消息队列
            //autoDelete：没有人用队列后，是否要删除队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            //发送消息
            //参数: exchange, routingKey, props, body
            //exchange：交换机，若设置为空字符串，则使用默认交换机，默认交换机类型为direct
            //routingKey：路由键，若交换机类型为direct，则routingKey必须与队列名称相同
            //props：消息属性
            //body：消息内容
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
