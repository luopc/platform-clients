package com.luopc.platform.clients.mongodb.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "products")
@CompoundIndex(name = "category_price_idx", def = "{'category': 1, 'price': -1}")
public class Product {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    @Indexed
    private String category;

    private BigDecimal price;

    private Integer stock;

    private Boolean available;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
