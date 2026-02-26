package com.luopc.platform.clients.amq.dev;

import com.luopc.platform.clients.amq.quques.MarketDataEventTagConst;
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
public class MarketDataTopicSubscriberTest {

    @Test
    public void testSubscriber() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        //创建消费者1
        executorService.submit(() -> subscribe(MarketDataEventTagConst.Rates.SPOT_RATES_CHANGE_EVENT_QUEUE, MarketDataEventTagConst.Rates.SPOT_RATES));
        //创建消费者2
        executorService.submit(() -> subscribe(MarketDataEventTagConst.Market.MARKET_PRICES_CHANGE_EVENT_QUEUE, MarketDataEventTagConst.Market.MARKET_PRICES));
        //创建消费者3
        executorService.submit(() -> subscribe(MarketDataEventTagConst.Bank.BANK_QUOTE_CHANGE_EVENT_QUEUE, MarketDataEventTagConst.Bank.BANK_QUOTE));
        //创建消费者4
        executorService.submit(() -> subscribe(MarketDataEventTagConst.Rates.FORWARD_RATES_CHANGE_EVENT_QUEUE, MarketDataEventTagConst.Rates.FORWARD_RATES));
        // 等待消费者绑定再发送消息
        Thread.sleep(20 * 60 * 1000);
    }

    private void subscribe(String queueName, String topic) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("data.luopc.com");
        factory.setPort(5672);
        factory.setUsername("dev");
        factory.setPassword("Robin2023");
        factory.setVirtualHost("dev");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(MarketDataEventTagConst.MARKET_DATA_EXCHANGE, BuiltinExchangeType.TOPIC);

            //方式2. 创建指定队列
            /*
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(queueName, false, false, true, null);
            //进行交换机和队列绑定
            /* 参数：String queue, String exchange, String routingKey
             * 参数明细：
             * 1、queue 队列名称
             * 2、exchange 交换机名称
             * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
             */
            channel.queueBind(queueName, MarketDataEventTagConst.MARKET_DATA_EXCHANGE, topic);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("[" + exchange + "] Received Message is:'" + message + "'");
                }
            });
            Thread.sleep(20 * 60 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
