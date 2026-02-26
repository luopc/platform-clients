package com.luopc.platform.clients.amq.quques;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robin
 */
@Configuration
public class OrderDealEventTagConst {

    public static final String ORDER_DEAL_EXCHANGE = "order-deal-exchange";


    public static class Generation {
        public static final String BANK_ORDER_GENERATION = "bank.order.generation";
        public static final String BANK_ORDER_GENERATION_EVENT_QUEUE = "bank_order_generation_event_queue";
        public static final String CLIENT_ORDER_GENERATION = "client.order.generation";
        public static final String CLIENT_ORDER_GENERATION_EVENT_QUEUE = "client_order_generation_event_queue";

    }

    public static class ExchangeTrade {
        public static final String ORDER_MATCHED_RESULT = "order.exchanged.result";
        public static final String BANK_ORDER_EXCHANGED_CONFIRMATION = "bank_order_exchanged_confirmation";
        public static final String CLIENT_ORDER_EXCHANGED_CONFIRMATION = "client_order_exchanged_confirmation";

    }


    public static class UpStream {
        public static final String ORDER_BOOKING_EVENT_QUEUE = "order_booking_event_queue";
        public static final String ORDER_BOOKING = "order.booking.#";
        public static final String ORDER_BOOKING_NEW = "order.booking.new";
        public static final String ORDER_BOOKING_MODIFY = "order.booking.modify";
        public static final String ORDER_BOOKING_ALLOCATE = "order.booking.allocate";
        public static final String ORDER_BOOKING_MATCHING = "order.booking.matching";
        public static final String ORDER_BOOKING_MATCHED = "order.booking.matched";
        public static final String ORDER_BOOKING_CANCELLING = "order.booking.cancelling";
        public static final String ORDER_BOOKING_CANCELLED = "order.booking.cancelled";
        public static final String ORDER_BOOKING_EXCISING = "order.booking.excising";
        public static final String ORDER_BOOKING_EXCISED = "order.booking.excised";
        public static final String ORDER_BOOKING_EXPIRED = "order.booking.expired";
    }



    public static class Inside {
        public static final String ORDER_OPERATION_EVENT_QUEUE = "order_inside_operation_change_event_queue";
        public static final String ORDER_OPERATION = "order.operation.#";
        public static final String ORDER_OPERATION_NEW = "order.operation.new";
        public static final String ORDER_OPERATION_REPLACE = "order.operation.replace";
        public static final String ORDER_OPERATION_MODIFY = "order.operation.modify";
        public static final String ORDER_OPERATION_ALLOCATE = "order.operation.allocate";
        public static final String ORDER_OPERATION_MATCHING = "order.operation.matching";
        public static final String ORDER_OPERATION_MATCHED = "order.operation.matched";
        public static final String ORDER_OPERATION_CANCELLING = "order.operation.cancelling";
        public static final String ORDER_OPERATION_CANCELLED = "order.operation.cancelled";
        public static final String ORDER_OPERATION_EXCISING = "order.operation.excising";
        public static final String ORDER_OPERATION_EXCISED = "order.operation.excised";
        public static final String ORDER_OPERATION_EXPIRED = "order.operation.expired";
    }

    public static class OptionTrade {
        public static final String OPTION_ORDER_EVENT_QUEUE = "option_order_change_event_queue";
        public static final String OPTION_ORDER = "option.order.#";
    }

    public static class CashTrade {
        public static final String CASH_ORDER_EVENT_QUEUE = "cash_order_change_event_queue";
        public static final String CASH_ORDER = "cash.order.#";
    }

    public static class MetalTrade {
        public static final String METAL_ORDER_EVENT_QUEUE = "metal_order_change_event_queue";
        public static final String METAL_ORDER = "metal.order.#";
    }


    @Bean(ORDER_DEAL_EXCHANGE)
    public static Exchange orderDataExchange() {
        // durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(ORDER_DEAL_EXCHANGE).durable(true).build();
    }

}
