package com.luopc.platform.clients.amq.test;

import com.luopc.platform.clients.amq.util.RabbitConnectionUtil;
import com.luopc.platform.web.common.core.util.SequenceIdUtil;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 第六种模型(RPC):
 * <p>
 * 对于RPC请求，客户端发送一条带有两个属性的消息:replyTo,设置为仅为请求创建的匿名独占队列,和correlationId,设置为每个请求的惟一id值。
 * 请求被发送到rpc_queue队列。
 * RPC工作进程(即:服务器rpcServer)在队列上等待请求。当一个请求出现时，它执行任务,并使用replyTo字段中的队列将结果发回客户机。
 * 客户机在回应消息队列上等待数据。当消息出现时，它检查correlationId属性。如果匹配请求中的值，则向程序返回该响应数据。
 */
@Disabled
public class G_RPCQueueTest {

    //队列名称
    private static final String QUEUE = "rpc-queue";

    @Test
    public void testQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(this::rpcServer);
        executorService.submit(() -> {
            String msq = this.rpcCall("4");
            System.out.println("第4个斐波那契数是: " + msq);
        });
        executorService.submit(() -> {
            String msq = this.rpcCall("5");
            System.out.println("第5个斐波那契数是: " + msq);
        });
        executorService.submit(() -> {
            String msq = this.rpcCall("6");
            System.out.println("第6个斐波那契数是: " + msq);
        });
        executorService.submit(() -> {
            String msq = this.rpcCall("7");
            System.out.println("第7个斐波那契数是: " + msq);
        });
        Thread.sleep(5 * 1000);
    }

    private void rpcServer() {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /*
             * 定义队列 rpc_queue, 将从它接收请求信息
             *
             * 参数:
             * 1. queue, 对列名
             * 2. durable, 持久化
             * 3. exclusive, 排他
             * 4. autoDelete, 自动删除
             * 5. arguments, 其他参数属性
             */
            channel.queueDeclare(QUEUE, false, false, false, null);
            channel.queuePurge(QUEUE);//清除队列中的内容

            channel.basicQos(1);//一次只接收一条消息

            //消费者开始接收消息, 等待从 rpc_queue接收请求消息, 不自动确认
            channel.basicConsume(QUEUE, false, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //处理收到的数据(要求第几个斐波那契数)
                    String msg = new String(body, StandardCharsets.UTF_8);
                    System.out.println("received msg from client:" + msg + ", correlationId: " + properties.getCorrelationId());
                    int n = Integer.parseInt(msg);
                    //求出第n个斐波那契数
                    int r = fbnq(n);
                    String response = String.valueOf(r);

                    //设置发回响应的id, 与请求id一致, 这样客户端可以把该响应与它的请求进行对应
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    /*
                     * 发送响应消息
                     * 1. 默认交换机
                     * 2. 由客户端指定的,用来传递响应消息的队列名
                     * 3. 参数(关联id)
                     * 4. 发回的响应消息
                     */
                    channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                    //发送确认消息
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
            Thread.sleep(2000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected static int fbnq(int n) {
        if (n == 1 || n == 2) return 1;
        return fbnq(n - 1) + fbnq(n - 2);
    }

    private String rpcCall(String param) {
        ConnectionFactory factory = RabbitConnectionUtil.getConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            //自动生成对列名,非持久,独占,自动删除
            String replyQueueName = channel.queueDeclare().getQueue();
            //生成关联id
            String corrId = String.valueOf(SequenceIdUtil.nextId());
            System.out.println("[" + param + "] generate correlationId = " + corrId);
            /*设置两个参数:
             * 1. 请求和响应的关联id
             * 2. 传递响应数据的queue
             */
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            //向 rpc_queue 队列发送请求数据, 请求第n个斐波那契数
            channel.basicPublish("", QUEUE, props, param.getBytes(StandardCharsets.UTF_8));

            //用来保存结果的阻塞集合,取数据时,没有数据会暂停等待
            BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

            //接收响应数据的回调对象
            DeliverCallback deliverCallback = (consumerTag, message) -> {
                //如果响应消息的关联id,与请求的关联id相同,我们来处理这个响应数据
                if (message.getProperties().getCorrelationId().contentEquals(corrId)) {
                    //把收到的响应数据,放入阻塞集合
                    System.out.println("put response message [" + param + "] to correlationId: " + corrId);
                    response.offer(new String(message.getBody(), StandardCharsets.UTF_8));
                }
            };

            CancelCallback cancelCallback = consumerTag -> {
                System.out.println(consumerTag + "cancelled param");
            };

            //开始从队列接收响应数据
            channel.basicConsume(replyQueueName, true, deliverCallback, cancelCallback);
            //返回保存在集合中的响应数据
            return response.take();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return param;
    }
}
