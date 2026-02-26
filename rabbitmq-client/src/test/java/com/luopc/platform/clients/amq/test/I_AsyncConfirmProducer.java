package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 异步confirm
 * <p>
 * 异步confirm模式是在生产者客户端添加ConfirmListener回调接口，重写接口的handAck()和handNack()方法，分别用来处理RabbitMQ回传的Basic.Ack和Basic.Nack。
 * 这两个方法都有两个参数，第1个参数deliveryTag用来标记消息的唯一序列号，第2个参数multiple表示的是是否为多条确认,值为true代表是多个确认，值为false代表是单个确认。
 */
@Disabled
public class I_AsyncConfirmProducer {

    private final static String EXCHANGE_NAME = "async-confirm-exchange";

    public static void main(String[] args) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        //创建与RabbitMQ服务的TCP连接
        //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            // 创建一个Exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            int batchCount = 10;
            long msgCount = 1;
            SortedSet<Long> confirmSet = new TreeSet<>();
            channel.confirmSelect();
            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    System.out.println("Ack,SeqNo：" + deliveryTag + ",multiple：" + multiple);
                    if (multiple) {
                        confirmSet.headSet(deliveryTag - 1).clear();
                    } else {
                        confirmSet.remove(deliveryTag);
                    }
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    System.out.println("Nack,SeqNo：" + deliveryTag + ",multiple：" + multiple);
                    if (multiple) {
                        confirmSet.headSet(deliveryTag - 1).clear();
                    } else {
                        confirmSet.remove(deliveryTag);
                    }
                    // 注意这里需要添加处理消息重发的场景
                }
            });
            // 演示发送100个消息
            while (msgCount <= batchCount) {
                long nextSeqNo = channel.getNextPublishSeqNo();
                channel.basicPublish(EXCHANGE_NAME, "", null, "async confirm test".getBytes());
                confirmSet.add(nextSeqNo);
                msgCount = nextSeqNo;
            }
            Thread.sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
