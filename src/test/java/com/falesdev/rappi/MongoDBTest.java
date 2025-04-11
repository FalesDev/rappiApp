package com.falesdev.rappi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;

import static com.mongodb.assertions.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class MongoDBTest {
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeAll
    static void setUp() {
        mongoDBContainer.start();
    }

    @Test
    void testMongoDBConnection() {
        assertNotNull(mongoTemplate);
    }

    @AfterAll
    public static void teardown() {
        mongoDBContainer.stop();
    }
}
