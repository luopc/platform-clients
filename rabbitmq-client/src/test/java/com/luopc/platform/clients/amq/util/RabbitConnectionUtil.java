package com.luopc.platform.clients.amq.util;

import cn.hutool.core.lang.UUID;
import com.rabbitmq.client.ConnectionFactory;


public class RabbitConnectionUtil {

    public static ConnectionFactory getConnectionFactory() {
        //创建连接工程，下面给出的是默认的case
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("data.luopc.com");
        factory.setPort(5672);
        factory.setUsername("test");
        factory.setPassword("Test123");
        //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器
        factory.setVirtualHost("test");
        return factory;
    }

    public static void main(String[] args) {
        System.out.println(UUID.fastUUID());
    }

}
