package com.luopc.platform.clients.mongodb.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.luopc.platform.clients.mongodb.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "test_db";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://test:Test2021@data.luopc.com:27017/admin");
    }
}
