package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 第五种模型(Header)
 * <p>
 * header模式与routing不同的地方在于，header模式取消routingkey，使用header中的 key/value（键值对）匹配队列。
 */
@Disabled
public class F_HeaderTest {

    //交换机名称
    //交换机
    private static final String EXCHANGE_HEADER_INFORM = "exchange_header_inform";

    //队列名称
    private static final String QUEUE_HEADER_MAIL = "header-work-queue-mail";
    private static final String QUEUE_HEADER_WECHAT = "header-work-queue-wechat";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        //创建生产者
        //this.produce(20);
        //创建消费者1
        executorService.submit(() -> subscribe(QUEUE_HEADER_MAIL, "mail"));
        //创建消费者2
        executorService.submit(() -> subscribe(QUEUE_HEADER_WECHAT, "wechat"));

        // 等待消费者绑定再发送消息
        Thread.sleep(100);
        //创建生产者
        this.produce(20);
    }

    private void produce(int count) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //声明一个交换机
            channel.exchangeDeclare(EXCHANGE_HEADER_INFORM, BuiltinExchangeType.HEADERS);

            //方式2. 创建指定队列
            /*
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE_HEADER_MAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_HEADER_WECHAT, true, false, false, null);

            //进行交换机队列的绑定
            Map<String, Object> headers_email = new Hashtable<>();
            headers_email.put("inform_type", "mail");
            Map<String, Object> headers_wechat = new Hashtable<>();
            headers_wechat.put("inform_type", "wechat");
            channel.queueBind(QUEUE_HEADER_MAIL, EXCHANGE_HEADER_INFORM, "", headers_email);
            channel.queueBind(QUEUE_HEADER_WECHAT, EXCHANGE_HEADER_INFORM, "", headers_wechat);

            for (int i = 0; i < count; i++) {
                String message = "[" + i + "]send msg to user via wechat";
                Map<String, Object> headers = new Hashtable<>();
                //匹配wechat通知消费者绑定的header
                headers.put("inform_type", "wechat");
                AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder();
                properties.headers(headers);

                /* 向交换机发送消息String exchange, String routingKey, boolean mandatory, BasicProperties props, byte[] body
                 * 参数明细
                 * 1.交换机,如果不指定将会使用mq默认交换机
                 * 2.路由key,交换机根据路由key来将消息转发至指定的队列,如果使用默认的交换机,routingKey设置为队列名称
                 * 3.消息属性
                 * 4.消息内容
                 */
                channel.basicPublish(EXCHANGE_HEADER_INFORM, "", properties.build(), message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("Send to wechat: " + message);
            }

            for (int i = 0; i < count; i++) {
                String message = "[" + i + "]send msg to user via mail";
                Map<String, Object> headers = new Hashtable<>();
                //匹配email通知消费者绑定的header
                headers.put("inform_type", "mail");
                AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder();
                properties.headers(headers);

                /* 向交换机发送消息String exchange, String routingKey, boolean mandatory, BasicProperties props, byte[] body
                 * 参数明细
                 * 1.交换机,如果不指定将会使用mq默认交换机
                 * 2.路由key,交换机根据路由key来将消息转发至指定的队列,如果使用默认的交换机,routingKey设置为队列名称
                 * 3.消息属性
                 * 4.消息内容
                 */
                channel.basicPublish(EXCHANGE_HEADER_INFORM, "", properties.build(), message.getBytes(StandardCharsets.UTF_8));
                //System.out.println("Send to wechat: " + message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void subscribe(String queueName, String platform) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            //1.声明一个交换机
            channel.exchangeDeclare(EXCHANGE_HEADER_INFORM, BuiltinExchangeType.HEADERS);

            Map<String, Object> headers = new Hashtable<String, Object>();
            headers.put("inform_type", platform);
            channel.queueBind(queueName, EXCHANGE_HEADER_INFORM, "", headers);

            //2.监听队列
            /*声明队列String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
             *参数名称:
             * 1.队列名称
             * 2.是否持久化mq重启后队列还在
             * 3.是否独占连接,队列只允许在该连接中访问,如果连接关闭后就会自动删除了,设置true可用于临时队列的创建
             * 4.自动删除,队列不在使用时就自动删除,如果将此参数和exclusive参数设置为true时,就可以实现临时队列
             * 5.参数,可以设置一个队列的扩展参数,比如可以设置队列存活时间
             * */
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // 交换机
                    String exchange = envelope.getExchange();
                    // 路由key
                    String routingKey = envelope.getRoutingKey();
                    // 消息Id mq在channel中用来标识消息的id,可用于确认消息已接受
                    long deliveryTag = envelope.getDeliveryTag();
                    // 消息内容
                    String message = new String(body, StandardCharsets.UTF_8);

                    System.out.println(queueName + ", Received Message is:'" + message + "'");
                }
            });
            Thread.sleep(30 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
