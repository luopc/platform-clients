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
 * 第一种模型(直连)
 * P：生产者，也就是要发送消息的程序
 * C：消费者：消息的接受者，会一直等待消息到来。
 * queue：消息队列，类似一个邮箱，可以缓存消息；生产者向其中投递消息，消费者从其中取出消息。
 */
@Disabled
public class A_BaseProducerTest {

    //队列名称
    private static final String QUEUE = "queue-hello";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        //创建生产者
        executorService.submit(this::testProduce);
        //创建消费者1
        executorService.submit(this::testConsumer);
        Thread.sleep(5 * 1000);
    }

    private void testProduce() {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建与RabbitMQ服务的TCP连接
        //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /*
             * 声明队列，如果Rabbit中没有此队列将自动创建
             * param1:队列名称
             * param2:是否持久化
             * param3:队列是否独占此连接
             * param4:队列不再使用时是否自动删除此队列
             * param5:队列参数
             */
            channel.queueDeclare(QUEUE, true, false, false, null);
            String message = "Hello World, Test:" + System.currentTimeMillis();
            /*
             * 消息发布方法
             * param1：Exchange的名称，如果没有指定，则使用Default Exchange
             * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指息队列
             * param3:消息包含的属性
             * param4：消息体
             */
            channel.basicPublish("", QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("send message success");
            Thread.sleep(2 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void testConsumer() {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建与RabbitMQ服务的TCP连接
        //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /*
             * 监听队列
             * params: String queue, boolean autoAck, Consumer callback
             */
            channel.basicConsume(QUEUE, true, new DefaultConsumer(channel) {
                /*
                 * 消费者接收消息调用此方法
                 * @param consumerTag 消费者的标签，在channel.basicConsume()去指定
                 * @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志 (收到消息失败后是否需要重新发送)
                 * @param properties
                 * @param body
                 * @throws IOException
                 */
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //路由key
                    String routingKey = envelope.getRoutingKey();
                    //消息id
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String msg = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received Message is:'" + msg + "'");
                }
            });
            Thread.sleep(2 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
