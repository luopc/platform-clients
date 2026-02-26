package com.luopc.platform.clients.amq.quques;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author Robin
 */
public class StaticDataEventTagConst {

    public static final String STATIC_DATA_EXCHANGE = "static-data-exchange";

    public static class Counterparty {
        public static final String COUNTERPARTY_UPDATE_EVENT_QUEUE = "counterparty_update_event_queue";
        public static final String COUNTERPARTY_UPDATE = "#.counterparty.update.#";

    }

    @Bean(STATIC_DATA_EXCHANGE)
    public static Exchange staticDataExchange() {
        // durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(STATIC_DATA_EXCHANGE).durable(true).build();
    }

}
