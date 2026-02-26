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
 * 第三种模式（Fanout） 扇出 也称为广播
 * <p>
 * 在广播模式下，消息发送流程是这样的：
 * 1.可以有多个消费者
 * 2.每个消费者有自己的queue（队列）
 * 3.每个队列都要绑定到Exchange（交换机）
 * 4.生产者发送的消息，只能发送到交换机，交换机来决定要发给哪个队列，生产者无法决定。
 * 5.交换机把消息发送给绑定过的所有队列
 * 6.队列的消费者都能拿到消息。实现一条消息被多个消费者消费
 */
@Disabled
public class C_FanoutTest {

    //队列名称
    private static final String EXCHANGE_FANOUT_INFORM = "fanout-publish-subscribe-event";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        this.publish(1);
        //创建消费者1
        executorService.submit(() -> subscribe("wechat"));
        //创建消费者2
        executorService.submit(() -> subscribe("email"));
        // 等待消费者绑定再发送消息
        Thread.sleep(100);
        //创建生产者
        this.publish(5);


        //创建消费者3
        executorService.submit(() -> subscribe("SMS"));
        // 等待消费者绑定再发送消息
        Thread.sleep(100);
        //创建生产者
        this.publish(10);

    }

    private void subscribe(String platform) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);

            //方式1. 创建临时队列
            //String queue = channel.queueDeclare().getQueue();
            //方式2. 创建指定队列
            String queue = "fanout-subscriber-" + platform;

            channel.queueDeclare(queue, true, false, false, null);
            //绑定队列到交换机
            channel.queueBind(queue, EXCHANGE_FANOUT_INFORM, "");
            channel.basicConsume(queue, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println(platform + ", Received Message is: '" + new String(body, StandardCharsets.UTF_8) + "'");
                }
            });
            Thread.sleep(30 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void publish(int count) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //发送消息
            for (int i = 0; i < count; i++) {
                //消息内容
                String message = "[" + i + "] this is a new msg.";
                channel.basicPublish(EXCHANGE_FANOUT_INFORM, "", null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("send: " + message);
                //Thread.sleep( 1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
