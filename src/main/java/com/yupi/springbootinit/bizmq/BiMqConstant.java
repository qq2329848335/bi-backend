package com.yupi.springbootinit.bizmq;

public interface BiMqConstant {

    String BI_EXCHANGE_NAME = "bi_exchange";
    //工作队列
    String BI_WORK_QUEUE_NAME = "bi_queue";
    //工作队列绑定的路由键
    String BI_WORK_QUEUE_BING_ROUTING_KEY = "bi_routingKey";

    // 死信队列
    String BI_DEAD_LETTER_QUEUE_NAME = "bi_dead_letter_queue";
    // 死信队列绑定的路由键
    String BI_DEAD_LETTER_QUEUE_ROUTING_KEY = "bi_dead_letter_routingKey";
}
