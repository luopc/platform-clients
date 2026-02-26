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
 * 发送方确认机制是指生产者将信道设置成confirm（确认）模式，一旦信道进入confirm模式，所有在该信道上面发布的消息都会被指派一个唯一的ID（从1开始），一旦消息被投递到RabbitMQ服务器之后，RabbitMQ就会发送一个确认（Basic.Ack）给生产者（包含消息的唯一ID），这就使得生产者知晓消息已经正确到达了目的地了。
 * 如果RabbitMQ因为自身内部错误导致消息丢失，就会发送一条nack（Basic.Nack）命令，生产者应用程序同样可以在回调方法中处理该nack指令。
 * 如果消息和队列是可持久化的，那么确认消息会在消息写入磁盘之后发出。
 * 事务机制在一条消息发送之后会使发送端阻塞，以等待RabbitMQ的回应，之后才能继续发送下一条消息。
 * 相比之下，发送方确认机制最大的好处在于它是异步的，一旦发布一条消息。生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认后，生产者应用程序便可以通过回调方法来处理该确认消息。
 */
@Disabled
public class H_BaseProducerConfirmTest {

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
            channel.confirmSelect();
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
            if (channel.waitForConfirms()) {
                System.out.println("send message success");
            } else {
                System.out.println("send message failed");
                // do something else...
            }
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
