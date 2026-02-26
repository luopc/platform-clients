package com.luopc.platform.clients.amq.bean.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * @author by Robin
 * @className OrderBuilder
 * @description TODO
 * @date 2024/1/15 0015 23:02
 */
public class OrderBuilder {

    public static Order build() {
        Random random = new Random();
        Order order = new Order();
        order.setOrderId(random.nextInt(999999));
        order.setOrderName("淘宝订单" + random.nextInt(10));
        order.setAmount(random.nextInt(999));
        order.setValueDate(LocalDate.now());
        order.setCreationTime(LocalDateTime.now());
        return order;
    }
}
