package com.luopc.platform.clients.amq.quques;

import org.springframework.amqp.core.CustomExchange;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by Robin
 * @className DelayedEventQueueTagConst
 * @description 延迟队列配置类：负责声明队列、交换机；并绑定
 * @date 2024/1/18 0018 22:15
 */
public class DelayedEventQueueTagConst {

    /**
     * 交换机名
     */
    public static final String DELAYED_EXCHANGE_NAME = "delayed-exchange";

    public static class Trade {
        /**
         * 队列名
         */
        public static final String TRADE_GENERATION_DELAYED_QUEUE = "trade_generation_delayed_queue";
        /**
         * routingKey
         */
        public static final String DELAYED_TRADE_GENERATION = "delayed.trade.generation";
    }

    @Bean(DELAYED_EXCHANGE_NAME)
    public CustomExchange delayedExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, arguments);
    }

}
