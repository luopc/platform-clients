package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 批量confirm
 * <p>
 * 批量confirm模式是每发送一批消息后，调用channel.waitForConfirms()方法，等待服务器的确认返回，因此相比于5.1中的普通confirm模式，性能更好。
 * 但是不好的地方在于，如果出现返回Basic.Nack或者超时情况，生产者客户端需要将这一批次的消息全部重发，这样会带来明显的重复消息数量，如果消息经常丢失，批量confirm模式的性能应该是不升反降的。
 */
@Disabled
public class H_BatchConfirmProducer {
    private final static String EXCHANGE_NAME = "batch-confirm-exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建与RabbitMQ服务的TCP连接
        //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            // 创建一个Exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            int batchCount = 100;
            int msgCount = 0;
            BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(100);
            try {
                channel.confirmSelect();
                while (msgCount <= batchCount) {
                    String message = "batch confirm test";
                    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
                    // 将发送出去的消息存入缓存中，缓存可以是一个ArrayList或者BlockingQueue之类的
                    blockingQueue.add(message);
                    if (++msgCount >= batchCount) {
                        try {
                            if (channel.waitForConfirms()) {
                                // 将缓存中的消息清空
                                blockingQueue.clear();
                            } else {
                                // 将缓存中的消息重新发送
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            // 将缓存中的消息重新发送
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
