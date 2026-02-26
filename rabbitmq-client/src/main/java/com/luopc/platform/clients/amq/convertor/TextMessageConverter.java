package com.luopc.platform.clients.amq.convertor;

import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.messaging.converter.MessageConversionException;

/**
 * @author by Robin
 * @className TestMessageConverter
 * @description TODO
 * @date 2024/1/15 0015 0:18
 */
public class TextMessageConverter implements MessageConverter {

    @NotNull
    @Override
    public Message toMessage(Object object, @NotNull MessageProperties messageProperties) throws org.springframework.messaging.converter.MessageConversionException {
        return new Message(object.toString().getBytes(), messageProperties);
    }

    @NotNull
    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        return new String(message.getBody());
    }
}
