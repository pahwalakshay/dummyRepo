package com.mediaocean.platform.bi.data.sensor.config.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Arrays;

@Configuration
@EnableMongoAuditing
public class Configurations extends AbstractMongoClientConfiguration {

    @Value("${mongodb.datasource.userName:mediaplan_service_data}")
    private String username;
    @Value("${mongodb.datasource.password:qa2_mediaService!4data}")
    private String password;
    @Value("${mongodb.datasource.host:ny-mongo-db02}")
    private String host;
    @Value("${mongodb.datasource.dbName:mediaplan_service_qa2}")
    private String dbName;
    @Value("${mongodb.datasource.port:27017}")
    private String port;

    @Override
    public MongoClient mongoClient() {

        MongoCredential credential = MongoCredential.createScramSha1Credential(username,
                    dbName,
                    password.toCharArray());

        return MongoClients.create(
                MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(
                                Arrays.asList(
                                        new ServerAddress(
                                                host,
                                                Integer.parseInt(port)
                                        )
                                )
                        )
                )
                .credential(credential)
                .build());

    }

    @Override
    public String getDatabaseName() {
        return dbName;
    }

}


