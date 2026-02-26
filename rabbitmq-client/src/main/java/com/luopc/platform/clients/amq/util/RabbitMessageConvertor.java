package com.luopc.platform.clients.amq.util;

import com.luopc.platform.web.common.core.util.SimpleJsonUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.MediaType;

/**
 * @author by Robin
 * @className RabbitMessageConvertor
 * @description TODO
 * @date 2024/1/16 0016 18:14
 */
public class RabbitMessageConvertor {

    public Message toJsonMessage(Object entity) {
        String json = SimpleJsonUtil.writeJson(entity);
        MessageProperties jsonProperties = new MessageProperties();
        jsonProperties.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return new Message(json.getBytes(), jsonProperties);
    }

}
