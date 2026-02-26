package com.luopc.platform.clients.amq.util;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MsgProducer {

    public static void publishMsg(String exchange, BuiltinExchangeType exchangeType, String toutingKey, String message) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建连接
        try (Connection connection = factory.newConnection();
             //创建消息通道
             Channel channel = connection.createChannel()) {

            // 声明exchange中的消息为可持久化，不自动删除
            channel.exchangeDeclare(exchange, exchangeType, true, false, null);
            // 发布消息
            channel.basicPublish(exchange, toutingKey, null, message.getBytes());
            System.out.println("Sent '" + message + "'");
        } catch (IOException | TimeoutException ex) {
            log.error("Unable to publish msg due to exception", ex);
        }
    }
}
