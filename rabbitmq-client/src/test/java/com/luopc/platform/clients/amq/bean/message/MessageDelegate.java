package com.luopc.platform.clients.amq.bean.message;

import com.luopc.platform.clients.amq.bean.entity.Order;
import com.luopc.platform.clients.amq.bean.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author by Robin
 * @className MessageDelegate
 * @description TODO
 * @date 2024/1/14 0014 23:00
 */
@Slf4j
public class MessageDelegate {

    /**
     * 用来接收消息的，方法名称和签名必须是这个，这是框架默认的方法名；
     *
     * @param messageBody
     */
    public void handleMessage(byte[] messageBody) {
        log.info("默认方法, 消息内容: {}", new String(messageBody));
    }

    public void handleMessage(String messageBody) {
        log.info("String message, 消息内容: {}", messageBody);
    }

    public void handleMessage(Map messageBody) {
        log.info("Map message, 消息内容: {}", messageBody);
    }

    public void handleMessage(Order order) {
        log.info("Order message, 消息内容: {}", order);
    }

    public void onMessage(byte[] messageBody) {
        log.info("default onMessage, 消息内容: {}", new String(messageBody));
    }

    public void onMessage(String messageBody) {
        log.info("String onMessage, 消息内容: {}", messageBody);
    }

    public void onMessage(Order order) {
        log.info("Order onMessage, 消息内容: {}", order);
    }

    public void onMessage(User user) {
        System.out.println("---------onMessage---user-------------");
        System.out.println(user.toString());
    }

    public void onMessage(List<Order> orders) {
        log.info("---------onMessage---List<Order>-------------");
        orders.stream().forEach(order -> System.out.println(order));
    }

    public void onMessage(Map<String, Object> orderMaps) {
        log.info("-------onMessage---Map<String,Object>------------");
        orderMaps.keySet().forEach(key -> System.out.println(orderMaps.get(key)));
    }
}
