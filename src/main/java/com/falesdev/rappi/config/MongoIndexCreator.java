package com.falesdev.rappi.config;

import com.falesdev.rappi.domain.document.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class MongoIndexCreator implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        //Indexes for users
        mongoTemplate.indexOps(User.class).ensureIndex(
                new Index().on("email", Sort.Direction.ASC).unique()
        );
        mongoTemplate.indexOps(User.class).ensureIndex(
                new Index().on("phone", Sort.Direction.ASC).unique()
        );
    }
}
