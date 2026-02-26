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
 * 第四种模型(Routing)
 * <p>
 * Routing 之订阅模型-Direct(直连)
 * 在Fanout模式中，一条消息，会被所有订阅的队列都消费。但是，在某些场景下，我们希望不同的消息被不同的队列消费。这时就要用到Direct类型的Exchange。
 * <p>
 * 在Direct模型下：
 * 队列与交换机的绑定，不能是任意绑定了，而是要指定一个RoutingKey（路由key）
 * 消息的发送方在 向 Exchange发送消息时，也必须指定消息的 RoutingKey。
 * Exchange不再把消息交给每一个绑定的队列，而是根据消息的Routing Key进行判断，只有队列的Routingkey与消息的 Routing key完全一致，才会接收到消息
 */
@Disabled
public class D_RoutingDirectTest {

    private static final String EXCHANGE_DIRECT = "test-publish-direct-event";
    private static final String QUEUE_DIRECT_MAIL = "queue-publish-direct-mail";
    private static final String QUEUE_DIRECT_WECHAT = "queue-publish-direct-wechat";
    private static final String MESSAGE_DIRECT_MAIL = "queue-message-mail";
    private static final String MESSAGE_DIRECT_WECHAT = "queue-message-wechat";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        //创建生产者
        this.publish(20);

        //创建消费者1
        executorService.submit(() -> subscribe(QUEUE_DIRECT_MAIL, MESSAGE_DIRECT_MAIL));
        //创建消费者2
        executorService.submit(() -> subscribe(QUEUE_DIRECT_WECHAT, MESSAGE_DIRECT_WECHAT));
        // 等待消费者绑定再发送消息
        Thread.sleep(100);
    }

    private void subscribe(String queueName, String platform) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT);

            //方式2. 创建指定队列
            /*
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(queueName, true, false, false, null);
            //进行交换机和队列绑定
            /* 参数：String queue, String exchange, String routingKey
             * 参数明细：
             * 1、queue 队列名称
             * 2、exchange 交换机名称
             * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
             */
            channel.queueBind(queueName, EXCHANGE_DIRECT, platform);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println(platform + ", [" + exchange + "]Received Message is:'" + message + "'");
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
            channel.exchangeDeclare(EXCHANGE_DIRECT, BuiltinExchangeType.DIRECT);
            /*
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE_DIRECT_MAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_DIRECT_WECHAT, true, false, false, null);

            /*
             * 参数明细：
             * 1、queue 队列名称
             * 2、exchange 交换机名称
             * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
             */
            channel.queueBind(QUEUE_DIRECT_MAIL, EXCHANGE_DIRECT, MESSAGE_DIRECT_MAIL);
            channel.queueBind(QUEUE_DIRECT_WECHAT, EXCHANGE_DIRECT, MESSAGE_DIRECT_WECHAT);

            //发送消息
            //参数：String exchange, String routingKey, BasicProperties props, byte[] body
            /*
             * 参数明细：
             * 1、exchange，交换机，如果不指定将使用mq的默认交换机（设置为""）
             * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认交换机，routingKey设置为队列的名称
             * 3、props，消息的属性
             * 4、body，消息内容
             */
            for (int i = 0; i < count; i++) {
                //发送消息的时候指定routingKey
                String message = "[" + i + "]send message via mail";
                channel.basicPublish(EXCHANGE_DIRECT, MESSAGE_DIRECT_MAIL, null, message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("send to mq " + message);
            }
            for (int i = 0; i < count; i++) {
                //发送消息的时候指定routingKey
                String message = "[" + i + "]send message via wechat";
                channel.basicPublish(EXCHANGE_DIRECT, MESSAGE_DIRECT_WECHAT, null, message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("send to mq " + message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
