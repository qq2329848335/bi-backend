package com.yupi.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * https://www.rabbitmq.com/tutorials/tutorial-six-java
 * 客户端代码稍微复杂一些：
 * 1.我们建立联系和渠道。
 * 2.我们的call方法发出实际的 RPC 请求。
 * 3.在这里，我们首先生成一个唯一的correlationId 数字并保存它 - 我们的消费者回调将使用该值来匹配适当的响应。
 * 4.然后，我们为回复创建一个专用的独占队列并订阅它。
 * 5.接下来，我们发布请求消息，具有两个属性： replyTo和correlationId。
 * 6.此时我们可以坐下来等待适当的响应到来。
 * 7.由于我们的消费者交付处理是在单独的线程中进行的，因此我们需要main在响应到达之前暂停线程。使用CompletableFuture是一种可能的解决方案。
 * 8.消费者正在做一项非常简单的工作，对于每一条消费的响应消息，它都会检查该消息是否是correlationId 我们正在寻找的消息。如果是这样，则完成CompletableFuture.
 * 9.同时main线程正在等待CompletableFuture完成。
 * 10.最后，我们将响应返回给用户。
 */
public class RPCClient implements AutoCloseable {
    // 定义与RabbitMQ的连接和信道
    private Connection connection;
    private Channel channel;
    // RPC请求队列的名称
    private String requestQueueName = "rpc_queue";

    // 构造函数，建立与RabbitMQ的连接和信道
    public RPCClient() throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置RabbitMQ服务器地址
        factory.setHost("localhost");

        // 创建连接
        connection = factory.newConnection();
        // 创建信道
        channel = connection.createChannel();
    }

    // 主函数，用于测试RPC客户端
    public static void main(String[] argv) {
        try (RPCClient fibonacciRpc = new RPCClient()) { // 创建RPC客户端实例
            // 循环请求斐波那契数
            for (int i = 0; i < 32; i++) {
                String i_str = Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");
                // 调用远程过程
                String response = fibonacciRpc.call(i_str);
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) { // 捕获并处理可能的异常
            e.printStackTrace();
        }
    }

    // 远程过程调用方法
    public String call(String message) throws IOException, InterruptedException, ExecutionException {
        // 生成唯一的相关ID
        final String corrId = UUID.randomUUID().toString();

        // 声明一个临时的回复队列
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId) // 设置相关ID
                .replyTo(replyQueueName) // 设置回复队列名称
                .build(); // 构建消息属性

        // 发布消息到请求队列，请求计算斐波那契数
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        // 使用CompletableFuture来异步接收响应
        final CompletableFuture<String> response = new CompletableFuture<>();

        // 监听回复队列，当接收到相关ID匹配的消息时，使用CompletableFuture.complete来完成future
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        // 阻塞直到future完成，获取响应结果
        String result = response.get();
        channel.basicCancel(ctag); // 取消消息监听
        return result; // 返回计算结果
    }

    // 关闭连接和信道，释放资源
    public void close() throws IOException {
        connection.close();
    }
}
