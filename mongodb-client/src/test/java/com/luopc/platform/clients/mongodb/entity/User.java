package com.luopc.platform.clients.mongodb.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String email;

    private String fullName;

    private Integer age;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
