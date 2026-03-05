package com.luopc.platform.clients.amq.config;

import com.luopc.platform.clients.amq.convertor.TextMessageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author by Robin
 * @className RabbitCommonConfig
 * @description RabbitCommonConfig
 * @date 2024/1/4 0004 19:59
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitCommonConfig {

    private RabbitProperties properties;

    public static final String LISTENER_FACTORY = "rabbitListenerContainerFactory";

    /**
     * RabbitMQ连接池，从配置文件读取参数
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost(properties.getHost());
        cachingConnectionFactory.setPort(properties.getPort());
        cachingConnectionFactory.setUsername(properties.getUsername());
        cachingConnectionFactory.setPassword(properties.getPassword());
        cachingConnectionFactory.setVirtualHost(properties.getVirtualHost());
        //开启连接池的ReturnCallBack支持
        cachingConnectionFactory.setPublisherReturns(properties.isPublisherReturns());
        //开启连接池的Publisher-confirm-type支持
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return cachingConnectionFactory;
    }

    /**
     * RabbitListener使用连接池，使用连接池时 spring.rabbitmq.listener.simple 配置不生效
     */
    @Bean(LISTENER_FACTORY)
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //关闭自动ACK
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //使用连接池
        factory.setConnectionFactory(connectionFactory);
        //设置QOS
        factory.setPrefetchCount(1);
        //设置消息转换器为JSON
        factory.setMessageConverter(getContentTypeDelegatingMessageConverter());
        return factory;
    }

    /**
     * 消息转换器
     *
     * @return
     */
    public ContentTypeDelegatingMessageConverter getContentTypeDelegatingMessageConverter() {
        ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter = new ContentTypeDelegatingMessageConverter();

        TextMessageConverter textMessageConverter = new TextMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("html/text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("xml/text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("text/plain", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("application/xml", textMessageConverter);

        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("json", jackson2JsonMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("application/json", jackson2JsonMessageConverter);

        return contentTypeDelegatingMessageConverter;
    }


}
