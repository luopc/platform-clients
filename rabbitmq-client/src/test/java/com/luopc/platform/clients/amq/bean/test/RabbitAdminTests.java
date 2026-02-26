package com.luopc.platform.clients.amq.bean.test;

import com.luopc.platform.clients.amq.bean.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

/**
 * @author by Robin
 * @className RabbitAdminTests
 * @description TODO
 * @date 2024/1/14 0014 23:15
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RabbitMQConfig.class)
public class RabbitAdminTests {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    public void testAdmin() throws Exception {
        rabbitAdmin.declareExchange(new DirectExchange(RabbitMQConfig.SPRING_DIRECT_EXCHANGE, false, false));

        rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.SPRING_TOPIC_EXCHANGE, false, false));

        rabbitAdmin.declareExchange(new FanoutExchange(RabbitMQConfig.SPRING_FANOUT_EXCHANGE, false, false));

        rabbitAdmin.declareQueue(new Queue(RabbitMQConfig.TEST_DIRECT_QUEUE, false));

        rabbitAdmin.declareQueue(new Queue(RabbitMQConfig.TEST_TOPIC_QUEUE, false));

        rabbitAdmin.declareQueue(new Queue(RabbitMQConfig.TEST_FANOUT_QUEUE, false));

        rabbitAdmin.declareBinding(new Binding(RabbitMQConfig.TEST_DIRECT_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMQConfig.SPRING_DIRECT_EXCHANGE, "direct", new HashMap<>()));

        rabbitAdmin.declareBinding(
                BindingBuilder
                        .bind(new Queue(RabbitMQConfig.TEST_TOPIC_QUEUE, false))                                //直接创建队列
                        .to(new TopicExchange(RabbitMQConfig.SPRING_TOPIC_EXCHANGE, false, false))    //直接创建交换机 建立关联关系
                        .with("user.#"));   //指定路由Key

        rabbitAdmin.declareBinding(
                BindingBuilder
                        .bind(new Queue(RabbitMQConfig.TEST_FANOUT_QUEUE, false))
                        .to(new FanoutExchange(RabbitMQConfig.SPRING_FANOUT_EXCHANGE, false, false)));

        //清空队列数据
        rabbitAdmin.purgeQueue(RabbitMQConfig.TEST_TOPIC_QUEUE, false);
    }
}
