package com.luopc.platform.clients.activemq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerService {

    @JmsListener(destination = "itbeienTopic")
    public void processMessage(String content) {
        log.info("消费者消费消息:{}",content);
    }
}
