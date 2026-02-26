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
 * Routing 之订阅模型-Topic
 * <p>
 * Topic类型的Exchange与Direct相比，都是可以根据RoutingKey把消息路由到不同的队列。
 * 只不过Topic类型Exchange可以让队列在绑定Routing key 的时候使用通配符！
 * 这种模型Routingkey 一般都是由一个或多个单词组成，多个单词之间以”.”分割，例如： item.insert
 */
@Disabled
public class E_RoutingTopicTest {


    private static final String EXCHANGE_TOPIC = "test-publish-topic-event";
    private static final String QUEUE_TOPIC_MAIL = "queue-publish-topic-mail";
    private static final String QUEUE_TOPIC_WECHAT = "queue-publish-topic-wechat";
    private static final String MESSAGE_TOPIC_MAIL = "topic.#.mail.#";
    private static final String MESSAGE_TOPIC_WECHAT = "topic.#.wechat.#";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        //创建生产者
        this.publish(20);

        //创建消费者1
        executorService.submit(() -> subscribe(QUEUE_TOPIC_MAIL, MESSAGE_TOPIC_MAIL));
        //创建消费者2
        executorService.submit(() -> subscribe(QUEUE_TOPIC_WECHAT, MESSAGE_TOPIC_WECHAT));
        // 等待消费者绑定再发送消息
        Thread.sleep(100);


    }

    private void subscribe(String queueName, String topic) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC);

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
            channel.queueBind(queueName, EXCHANGE_TOPIC, topic);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println(topic + ", [" + exchange + "]Received Message is:'" + message + "'");
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
            channel.exchangeDeclare(EXCHANGE_TOPIC, BuiltinExchangeType.TOPIC, true);
            /*
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE_TOPIC_MAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_TOPIC_WECHAT, true, false, false, null);

            /*
             * 参数明细：
             * 1、queue 队列名称
             * 2、exchange 交换机名称
             * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
             */
            channel.queueBind(QUEUE_TOPIC_MAIL, EXCHANGE_TOPIC, MESSAGE_TOPIC_MAIL);
            channel.queueBind(QUEUE_TOPIC_WECHAT, EXCHANGE_TOPIC, MESSAGE_TOPIC_WECHAT);

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
                //设置消息的投递模式为2，即代表消息持久化。
                AMQP.BasicProperties props = new AMQP.BasicProperties().builder().deliveryMode(2).build();
                channel.basicPublish(EXCHANGE_TOPIC, "topic.mail", props, message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("send to mq " + message);
            }
            for (int i = 0; i < count; i++) {
                //发送消息的时候指定routingKey
                String message = "[" + i + "]send message via wechat";
                channel.basicPublish(EXCHANGE_TOPIC, "topic.wechat", null, message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("send to mq " + message);
            }
            for (int i = 0; i < count; i++) {
                //发送消息的时候指定routingKey
                String message = "[" + i + "]send message via wechat and mail";
                channel.basicPublish(EXCHANGE_TOPIC, "topic.wechat.mail", null, message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("send to mq " + message);
            }
            Thread.sleep(10 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
