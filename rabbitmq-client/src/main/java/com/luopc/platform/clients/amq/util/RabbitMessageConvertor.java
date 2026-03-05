package com.luopc.platform.clients.amq.util;

import cn.hutool.json.JSONUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * @author by Robin
 * @className RabbitMessageConvertor
 * @description TODO
 * @date 2024/1/16 0016 18:14
 */
public class RabbitMessageConvertor {

    public Message toJsonMessage(Object entity) {
        String json = JSONUtil.toJsonStr(entity);
        MessageProperties jsonProperties = new MessageProperties();
        jsonProperties.setContentType("application/json");
        return new Message(json.getBytes(), jsonProperties);
    }

}
