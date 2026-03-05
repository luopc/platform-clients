package com.luopc.platform.clients.amq.bean.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luopc.platform.clients.amq.bean.config.RabbitMQConfig;
import com.luopc.platform.clients.amq.bean.entity.Order;
import com.luopc.platform.clients.amq.bean.entity.OrderBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by Robin
 * @className RabbitApplicationTests
 * @description TODO
 * @date 2024/1/14 0014 23:01
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RabbitMQConfig.class)
public class RabbitApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage() throws Exception {

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getHeaders().put("desc", "信息描述..");
        messageProperties.getHeaders().put("type", "自定义消息类型..");
        Message message = new Message("Hello RabbitMQ".getBytes(), messageProperties);

        rabbitTemplate.convertAndSend(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.amqp", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                System.err.println("------添加额外的设置---------");
                message.getMessageProperties().getHeaders().put("desc", "额外修改的信息描述");
                message.getMessageProperties().getHeaders().put("attr", "额外新加的属性");
                return message;
            }
        });

    }

    @Test
    public void testSendMessage2() throws Exception {

        MessageProperties messageProperties = new MessageProperties();
        Message message = new Message("发送default 消息".getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.abc", message);

        messageProperties.setContentType("text/plain");
        message = new Message("发送String 消息".getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.abc", message);

        rabbitTemplate.convertAndSend(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.amqp", "amqp object message send!");
        rabbitTemplate.convertAndSend(RabbitMQConfig.SPRING_TEST_EXCHANGE, "rabbit.abc", "rabbit object message send!");

        rabbitTemplate.convertAndSend(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.amqp", "end object message send!");

        Order order = OrderBuilder.build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(order);
        log.info("order 2 json: " + json);

        MessageProperties jsonProperties = new MessageProperties();
        jsonProperties.setContentType("application/json");
        jsonProperties.getHeaders().put("__TypeId__", "order");
        Message josonMessage = new Message(json.getBytes(), jsonProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.abc", josonMessage);

        order = OrderBuilder.build();
        json = mapper.writeValueAsString(order);
        MessageProperties objectProperties = new MessageProperties();
        objectProperties.setContentType("application/json");
        objectProperties.getHeaders().put("__TypeId__", "order");
        Message orderMessage = new Message(json.getBytes(), objectProperties);
        log.info("order 2 json: " + json);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.abc", orderMessage);


    }

    @Test
    public void testSendJsonMessage() throws Exception {

        Order order = OrderBuilder.build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(order);
        log.info("order 2 json: " + json);

        MessageProperties jsonProperties = new MessageProperties();
        jsonProperties.setContentType("application/json");
        jsonProperties.getHeaders().put("__TypeId__", "order");
        Message josonMessage = new Message(json.getBytes(), jsonProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.abc", josonMessage);
    }

    @Test
    public void testSendJsonListMessage() throws Exception {
        List<Order> orderList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        orderList.add(OrderBuilder.build());
        orderList.add(OrderBuilder.build());

        String json = mapper.writeValueAsString(orderList);
        log.info("order 2 json: " + json);

        MessageProperties jsonProperties = new MessageProperties();
        jsonProperties.setContentType("application/json");
        jsonProperties.getHeaders().put("__TypeId__", "java.util.List");
        jsonProperties.getHeaders().put("__ContentTypeId__", "order");

        Message josonMessage = new Message(json.getBytes(), jsonProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.list", josonMessage);
    }

    @Test
    public void testSendObjectMessage() throws Exception {
        Order order = OrderBuilder.build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(order);
        log.info("order 2 json: " + json);

        MessageProperties objectProperties = new MessageProperties();
        objectProperties.setContentType("application/json");
        objectProperties.getHeaders().put("__TypeId__", "order");
        Message orderMessage = new Message(json.getBytes(), objectProperties);

        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.order", orderMessage);
    }

}
