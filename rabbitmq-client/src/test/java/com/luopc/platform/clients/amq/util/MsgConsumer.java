package com.luopc.platform.clients.amq.util;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


@Slf4j
public class MsgConsumer {

    public static void consumerMsg(String exchange, String queue, String routingKey) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建连接
        try (Connection connection = factory.newConnection();
             //创建消息通道
             Channel channel = connection.createChannel()) {

            /*
             * 声明队列，如果Rabbit中没有此队列将自动创建
             * param1:队列名称
             * param2:是否持久化
             * param3:队列是否独占此连接
             * param4:队列不再使用时是否自动删除此队列
             * param5:队列参数
             */
            channel.queueDeclare(queue, true, false, false, null);
            //绑定队列到交换机
            channel.queueBind(queue, exchange, routingKey);
            log.info("[*] Waiting for message. To exist press CTRL+C");

            Consumer callback = new DefaultConsumer(channel) {
                /*
                 * 消费者接收消息调用此方法
                 * @param consumerTag 消费者的标签，在channel.basicConsume()去指定
                 * @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志 (收到消息失败后是否需要重新发送)
                 * @param properties
                 * @param body
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //路由key
                    String routingKey = envelope.getRoutingKey();
                    //消息id
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body, StandardCharsets.UTF_8);
                    try {
                        log.info(" [x] Received '" + message);
                    } finally {
                        log.info(" [x] Done");
                        channel.basicAck(deliveryTag, false);
                    }
                }
            };
            /*
             * 监听队列
             * params: String queue, boolean autoAck, Consumer callback
             */
            channel.basicConsume(queue, false, callback);

        } catch (IOException | TimeoutException ex) {
            log.error("Unable to publish msg due to exception", ex);
        }
    }
}
