package com.luopc.platform.clients.amq.bean.message;

/**
 * @author by Robin
 * @className FooMessageListener
 * @description TODO
 * @date 2024/1/15 0015 22:29
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FooMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        String messageBody = new String(message.getBody());
        log.info(" [x] Received '" + messageBody + "'");
    }
}
