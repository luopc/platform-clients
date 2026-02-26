package com.luopc.platform.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;


@Slf4j
@EnableJms
@SpringBootApplication
public class ActiveMQApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActiveMQApplication.class, args);
    }
}
