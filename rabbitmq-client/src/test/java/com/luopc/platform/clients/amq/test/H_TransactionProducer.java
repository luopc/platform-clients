package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Disabled;

/**
 * RabbitMQ 客户端中与事务机制相关的方法有以下3个：
 * <p>
 * 1.channel.txSelect：用于将当前的信道设置成事务模式
 * 2.channel.txCommit：用于提交事务
 * 3.channel.txRollback：用于回滚事务
 */
@Disabled
public class H_TransactionProducer {
    private final static String QUEUE_NAME = "transaction-queue-hello";

    public static void main(String[] args) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建与RabbitMQ服务的TCP连接
        //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            // 指定一个队列,不存在的话自动创建
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            channel.txSelect();

            // 发送消息
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            channel.txCommit();
            System.out.println(" [x] Sent '" + message + "'");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
