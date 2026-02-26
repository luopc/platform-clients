package com.luopc.platform.clients.amq.bean.entity;

import lombok.Data;

/**
 * @author by Robin
 * @className User
 * @description TODO
 * @date 2024/1/15 0015 23:07
 */
@Data
public class User {
    private int userId;
    private int age;
    private String username;
    private String password;
}
