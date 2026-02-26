package com.luopc.platform.clients.amq.quques;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robin
 */
@Configuration
public class TradeDataEventTagConst {

    public static final String TRADE_DATA_EXCHANGE = "trade-data-exchange";


    public static class Update {
        public static final String TRADE_UPDATE_EVENT_QUEUE = "trade_update_event_queue";
        public static final String TRADE_UPDATE = "trade.update.#";
        public static final String TRADE_UPDATE_NEW = "trade.update.new";
        public static final String TRADE_UPDATE_MODIFY = "trade.update.modify";
        public static final String TRADE_UPDATE_ALLOCATE = "trade.update.allocate";
        public static final String TRADE_UPDATE_MATCHING = "trade.update.matching";
        public static final String TRADE_UPDATE_MATCHED = "trade.update.matched";
        public static final String TRADE_UPDATE_CANCELLING = "trade.update.cancelling";
        public static final String TRADE_UPDATE_CANCELLED = "trade.update.cancelled";
        public static final String TRADE_UPDATE_EXCISING = "trade.update.excising";
        public static final String TRADE_UPDATE_EXCISED = "trade.update.excised";
        public static final String TRADE_UPDATE_EXPIRED = "trade.update.expired";
    }



    public static class Inside {
        public static final String TRADE_OPERATION_EVENT_QUEUE = "trade_inside_operation_change_event_queue";
        public static final String TRADE_OPERATION = "trade.operation.#";
        public static final String TRADE_OPERATION_NEW = "trade.operation.new";
        public static final String TRADE_OPERATION_REPLACE = "trade.operation.replace";
        public static final String TRADE_OPERATION_MODIFY = "trade.operation.modify";
        public static final String TRADE_OPERATION_ALLOCATE = "trade.operation.allocate";
        public static final String TRADE_OPERATION_MATCHING = "trade.operation.matching";
        public static final String TRADE_OPERATION_MATCHED = "trade.operation.matched";
        public static final String TRADE_OPERATION_CANCELLING = "trade.operation.cancelling";
        public static final String TRADE_OPERATION_CANCELLED = "trade.operation.cancelled";
        public static final String TRADE_OPERATION_EXCISING = "trade.operation.excising";
        public static final String TRADE_OPERATION_EXCISED = "trade.operation.excised";
        public static final String TRADE_OPERATION_EXPIRED = "trade.operation.expired";
    }

    public static class OptionTrade {
        public static final String OPTION_TRADE_EVENT_QUEUE = "option_trade_change_event_queue";
        public static final String OPTION_TRADE = "option.trade.#";
    }

    public static class CashTrade {
        public static final String CASH_TRADE_EVENT_QUEUE = "cash_trade_change_event_queue";
        public static final String CASH_TRADE = "cash.trade.#";
    }

    public static class MetalTrade {
        public static final String METAL_TRADE_EVENT_QUEUE = "metal_trade_change_event_queue";
        public static final String METAL_TRADE = "metal.trade.#";
    }


    @Bean(TRADE_DATA_EXCHANGE)
    public static Exchange tradeDataExchange() {
        // durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(TRADE_DATA_EXCHANGE).durable(true).build();
    }

}
