package com.luopc.platform.clients.amq.bean.config;

import com.luopc.platform.clients.amq.bean.entity.User;
import com.luopc.platform.clients.amq.convertor.TextMessageConverter;
import com.luopc.platform.clients.amq.bean.entity.Order;
import com.luopc.platform.clients.amq.bean.message.MessageDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by Robin
 * @className RabbitMQConfig
 * @description TODO
 * @date 2024/1/14 0014 22:49
 */
@Slf4j
@Configuration
@ComponentScan({"com.luopc.platform.cloud.amq.bean.*"})
public class RabbitMQConfig {

    public static final String SPRING_DIRECT_EXCHANGE = "spring-direct-exchange";
    public static final String SPRING_TOPIC_EXCHANGE = "spring-topic-exchange";
    public static final String SPRING_FANOUT_EXCHANGE = "spring-fanout-exchange";
    public static final String SPRING_TEST_EXCHANGE = "spring-test-exchange";


    public static final String TEST_DIRECT_QUEUE = "test_direct_queue";
    public static final String TEST_TOPIC_QUEUE = "test_topic_queue";
    public static final String TEST_FANOUT_QUEUE = "test_fanout_queue";
    public static final String SIMPLE_MESSAGE_QUEUE = "simple_message_queue";
    public static final String RABBIT_MESSAGE_QUEUE = "rabbit_message_queue";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("data.luopc.com");
        factory.setPort(5672);
        factory.setUsername("test");
        factory.setPassword("Test123");
        //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器
        factory.setVirtualHost("test");
        return factory;
    }

    @Bean(SPRING_TEST_EXCHANGE)
    public TopicExchange springExchange() {
        return new TopicExchange(SPRING_TEST_EXCHANGE, true, false);
    }

    @Bean(SIMPLE_MESSAGE_QUEUE)
    public Queue simpleMessageQueue() {
        return new Queue(SIMPLE_MESSAGE_QUEUE, false, false, true);
    }

    @Bean
    public Binding bindingSimpleMessageQueue(@Qualifier(SPRING_TEST_EXCHANGE) Exchange exchange,
                                             @Qualifier(SIMPLE_MESSAGE_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("spring.*").noargs();
    }

    @Bean(RABBIT_MESSAGE_QUEUE)
    public Queue rabbitMessageQueue() {
            return new Queue(RABBIT_MESSAGE_QUEUE, false, false, true);
    }

    @Bean
    public Binding bindingRabbitMessageQueue(@Qualifier(SPRING_TEST_EXCHANGE) Exchange exchange,
                                             @Qualifier(RABBIT_MESSAGE_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("rabbit.*").noargs();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(simpleMessageQueue(), rabbitMessageQueue());
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
//
//        container.setConsumerTagStrategy(new ConsumerTagStrategy() {
//            @Override
//            public String createConsumerTag(String queue) {
//                return queue + "_" + UUID.randomUUID().toString();
//            }
//        });
//
//        container.setMessageListener(new ChannelAwareMessageListener() {
//            @Override
//            public void onMessage(Message message, Channel channel) throws Exception {
//                String msg = new String(message.getBody());
//                log.info("----------消费者: {}", msg);
//            }
//        });

        /**
         * 设置自定义的方法监听；
         */
        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());

        //全局的转换器:
        ContentTypeDelegatingMessageConverter convert = new ContentTypeDelegatingMessageConverter();
        TextMessageConverter textConvert = new TextMessageConverter();

        Jackson2JsonMessageConverter jsonConvert = new Jackson2JsonMessageConverter();
        //消费端配置映射
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("order", Order.class);
        idClassMapping.put("user", User.class);

        DefaultJackson2JavaTypeMapper jackson2JavaTypeMapper = new DefaultJackson2JavaTypeMapper();
        jackson2JavaTypeMapper.setIdClassMapping(idClassMapping);
        jackson2JavaTypeMapper.addTrustedPackages("com.luopc.platform.cloud.amq.bean.entity");
        jsonConvert.setJavaTypeMapper(jackson2JavaTypeMapper);
        adapter.setMessageConverter(jsonConvert);

        convert.addDelegate("text", textConvert);
        convert.addDelegate("html/text", textConvert);
        convert.addDelegate("xml/text", textConvert);
        convert.addDelegate("text/plain", textConvert);
        convert.addDelegate(MediaType.APPLICATION_XML_VALUE, textConvert);
        convert.addDelegate("json", jsonConvert);
        convert.addDelegate(MediaType.APPLICATION_JSON_VALUE, jsonConvert);

        adapter.setDefaultListenerMethod("onMessage");
        adapter.setMessageConverter(convert);
        container.setMessageListener(adapter);

        /**
         * 添加 Message 转换器
         */
//        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
//        // 能把 json 转成 Java 对象的东西；
//        DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
//        javaTypeMapper.addTrustedPackages("com.luopc.platform.cloud.amq.bean.entity");
//
//        Map<String, Class<?>> idClassMapping = new HashMap<>();
//        idClassMapping.put("order", com.luopc.platform.cloud.amq.bean.entity.Order.class);
//        javaTypeMapper.setIdClassMapping(idClassMapping);
//
//        jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);

//        adapter.setMessageConverter(jackson2JsonMessageConverter);
//        container.setMessageListener(adapter);

        return container;

    }

    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(simpleMessageQueue());

        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
        //指定Json转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();


        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("order", Order.class);
        idClassMapping.put("user", User.class);

        DefaultJackson2JavaTypeMapper jackson2JavaTypeMapper = new DefaultJackson2JavaTypeMapper();
        jackson2JavaTypeMapper.setIdClassMapping(idClassMapping);

        jackson2JsonMessageConverter.setJavaTypeMapper(jackson2JavaTypeMapper);
        adapter.setMessageConverter(jackson2JsonMessageConverter);

        TextMessageConverter textMessageConverter = new TextMessageConverter();

        ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter = new ContentTypeDelegatingMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("html/text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("xml/text", textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("text/plain", textMessageConverter);

        contentTypeDelegatingMessageConverter.addDelegate("json", jackson2JsonMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("application/json", jackson2JsonMessageConverter);

//        contentTypeDelegatingMessageConverter.addDelegate("image/jpg",new JPGMessageConverter());
//        contentTypeDelegatingMessageConverter.addDelegate("image/jepg",new JPGMessageConverter());
//        contentTypeDelegatingMessageConverter.addDelegate("image/png",new JPGMessageConverter());


        adapter.setMessageConverter(contentTypeDelegatingMessageConverter);
        //设置处理器的消费消息的默认方法
        adapter.setDefaultListenerMethod("onMessage");
        container.setMessageListener(adapter);

        return container;
    }

}
