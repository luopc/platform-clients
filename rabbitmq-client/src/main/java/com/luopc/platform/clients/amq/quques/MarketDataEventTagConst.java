package com.luopc.platform.clients.amq.quques;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robin
 */

@Configuration
public class  MarketDataEventTagConst{

    public static final String MARKET_DATA_EXCHANGE = "market-data-exchange";

    public static class Rates {

        public static final String SPOT_RATES_CHANGE_EVENT_QUEUE = "spot_rates_change_event_queue";
        public static final String SPOT_RATES = "rates.spot.#";

        public static final String FORWARD_RATES_CHANGE_EVENT_QUEUE = "forward_rates_change_event_queue";
        public static final String FORWARD_RATES = "rates.forward.#";

    }

    public static class Market {
        public static final String MARKET_PRICES_CHANGE_EVENT_QUEUE = "market_prices_change_event_queue";
        public static final String CLIENT_MARKET_PRICES_SUBSCRIBE_QUEUE = "client_market_prices_subscribe_queue";
        public static final String MARKET_PRICES = "market.prices.#";
    }


    public static class Bank {
        public static final String BANK_QUOTE_CHANGE_EVENT_QUEUE = "bank_quote_change_event_queue";
        public static final String BTB_BANK_QUOTE_SUBSCRIBE_QUEUE = "btb_bank_quote_subscribe_queue";
        public static final String BANK_QUOTE = "bank.quote.#";
    }

    @Bean(MARKET_DATA_EXCHANGE)
    public static Exchange marketDataExchange() {
        // durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(MARKET_DATA_EXCHANGE).durable(false).build();
    }

}
