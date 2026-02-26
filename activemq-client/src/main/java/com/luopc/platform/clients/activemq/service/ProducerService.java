package com.luopc.platform.clients.activemq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProducerService {

    private final JmsTemplate jmsTemplate;

    public ProducerService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMsg(String msg){
        this.jmsTemplate.convertAndSend("itbeienTopic",msg);
        log.info("生产者生产消息...");
    }
}
