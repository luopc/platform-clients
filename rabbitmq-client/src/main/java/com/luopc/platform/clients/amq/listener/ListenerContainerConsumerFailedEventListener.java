package com.luopc.platform.clients.amq.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * @author by Robin
 * @className ListenerContainerConsumerFailedEventListener
 * @description 通过实现该接口可以处理异常信息，当遇到监听异常时，我们可以停止监听，然后重新监听队列
 * @date 2024/1/4 0004 13:14
 */
@Slf4j
@Component
public class ListenerContainerConsumerFailedEventListener implements ApplicationListener<ListenerContainerConsumerFailedEvent> {

    @Override
    public void onApplicationEvent(ListenerContainerConsumerFailedEvent event) {
        log.error("listener queue failed: {}", event);

        if (event.isFatal()) {
            log.error("reason: {}, detail.", event.getReason(), event.getThrowable());

            SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) event.getSource();
            String queueNames = Arrays.toString(container.getQueueNames());

            //重启
            try {
                // 暂停10s
                Thread.sleep(10000);

                try {
                    container.stop();
                } catch (Exception e) {
                    log.error("stop listener queue {} failed!", queueNames);
                }

                Assert.state(!container.isRunning(), "listener queue: " + container + " is running!");

                container.start();
                log.info("restart listener queue {} successes!", queueNames);
            } catch (Exception e) {
                log.error("restart listener queue {} failed!", queueNames, e);
            }
        }
    }
}

