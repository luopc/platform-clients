package com.luopc.platform.clients.amq.bean.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luopc.platform.clients.amq.bean.config.RabbitMQConfig;
import com.luopc.platform.clients.amq.bean.entity.Order;
import com.luopc.platform.clients.amq.bean.entity.OrderBuilder;
import com.luopc.platform.clients.amq.bean.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by Robin
 * @className MyRabbitMQConfigSendTest
 * @description TODO
 * @date 2024/1/15 0015 22:58
 */

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RabbitMQConfig.class)
public class MyRabbitMQConfigSendTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendOrder() throws Exception {
        Order order = OrderBuilder.build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(order);
        log.info("order 2 json: " + json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "order");
        Message message = new Message(json.getBytes(), messageProperties);

        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.order", message);
    }

    @Test
    public void sendUser() throws Exception {
        User user = new User();
        user.setUserId(1000);
        user.setAge(50);
        user.setUsername("zhihao.miao");
        user.setPassword("123343");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);
        log.info("user 2 json: " + json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "user");
        Message message = new Message(json.getBytes(), messageProperties);

        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.user", message);
    }

    @Test
    public void sendOrderList() throws Exception {
        List<Order> orderList = Arrays.asList(OrderBuilder.build(), OrderBuilder.build());

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(orderList);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "java.util.List");
        messageProperties.getHeaders().put("__ContentTypeId__", "order");


        Message message = new Message(json.getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.order", message);
    }

    @Test
    public void sendOrderMap() throws Exception {

        Map<String, Object> orderMaps = new HashMap<>();
        orderMaps.put("10", OrderBuilder.build());
        orderMaps.put("20", OrderBuilder.build());

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(orderMaps);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__", "java.util.Map");
        messageProperties.getHeaders().put("__KeyTypeId__", "java.lang.String");
        messageProperties.getHeaders().put("__ContentTypeId__", "order");


        Message message = new Message(json.getBytes(), messageProperties);
        rabbitTemplate.send(RabbitMQConfig.SPRING_TEST_EXCHANGE, "spring.map", message);
    }


}
