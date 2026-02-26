package com.luopc.platform.clients.amq.convertor;


import com.luopc.platform.web.common.core.util.SequenceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.messaging.converter.MessageConversionException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author by Robin
 * @className JPGMessageConverter
 * @description TODO
 * @date 2024/1/16 0016 8:39
 */
@Slf4j
public class ImageMessageConverter implements MessageConverter {
    @NotNull
    @Override
    public Message toMessage(@NotNull Object object, @NotNull MessageProperties messageProperties) throws MessageConversionException {
        return null;
    }

    @NotNull
    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        System.out.println("====JPGMessageConverter====");
        byte[] body = message.getBody();
        String fileName = SequenceIdUtil.shortId().toString();
        String path = "/Users/naeshihiroshi/Desktop/file/" + fileName + ".jpg";
        File file = new File(path);
        try {
            Files.copy(new ByteArrayInputStream(body), file.toPath());
        } catch (IOException e) {
            log.error("Unable to convert image msg");
        }
        return file;
    }
}

