package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 第二种模式（Work queues），也被称为（Task queues），任务模型。
 * 当消息处理比较耗时的时候，可能生产消息的速度会远远大于消息的消费速度。长此以往，消息就会堆积越来越多，无法及时处理。
 * 此时就可以使用work 模型：让多个消费者绑定到一个队列，共同消费队列中的消息。
 * 队列中的消息一旦消费，就会消失，因此任务是不会被重复执行的。
 */
@Disabled
public class B_WorkQueuesTest {


    //队列名称
    private static final String QUEUE = "queue-work-queue";

    @Test
    public void testQueue() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        //创建生产者
        this.produce(20);
        //创建消费者1
        executorService.submit(() -> consumer("A"));
        //创建消费者2
        executorService.submit(() -> consumer("B"));
    }

    private void produce(int count) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE, true, false, false, null);
            for (int i = 0; i < count; i++) {
                String message = "[" + i + "] Hello World, Test:" + System.currentTimeMillis();


                /*
                 * 消息发布方法
                 * param1：Exchange的名称，如果没有指定，则使用Default Exchange
                 * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指息队列
                 * param3:消息包含的属性
                 * param4：消息体
                 */
                channel.basicPublish("", QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("Send Message is:'" + message + "'");
//                Thread.sleep(10);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void consumer(String consumerName) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.basicConsume(QUEUE, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println(consumerName + ", Received Message is:'" + new String(body, StandardCharsets.UTF_8) + "'");
                }
            });
            Thread.sleep(30 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
