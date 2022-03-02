package com.mediaocean.platform.bi.data.sensor.config.kafka;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Configuration
@ConfigurationProperties("kafka")
public class KafkaTopicConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTopicConfiguration.class);
    private static final String TOPIC_NAME_TEMPLATE = "prisma.%s.%s";
    private List<TopicDetails> topics;
    private Set<String> topicNames = null; // Initialization post bean construction
    private String environment;

    @Inject
    public KafkaTopicConfiguration(@Value(Constants.PRISMA_ENVIRONMENT_NAME) String environment) {
        this.environment = environment;
    }

    public KafkaTopicConfiguration() {
        super();
    }

    @PostConstruct
    public void init() {
        this.topicNames = new HashSet<>(topics.size());
        this.topics.stream().filter(t -> t.isValid()).forEach(t -> this.topicNames.add(String.format(TOPIC_NAME_TEMPLATE, environment, t.getName().trim())));
        if (this.topics.stream().anyMatch(t -> !t.isValid())) {
            // If there are invalid topics, remove them from list and log error
            LOG.error("Found some invalid (NULL or Empty) names for topic. These topics would no be used.");
            this.topics = this.topics.stream().filter(t -> t.isValid()).collect(Collectors.toList());
        }
    }

    public boolean isValid(String topic) {
        return topicNames.contains(topic);
    }

    public void setTopics(List<TopicDetails> topics) {
        this.topics = topics;
    }

    public List<TopicDetails> getTopics() {
        return topics;
    }

    public static class TopicDetails {
        String name;
        int partitions = 3;
        short replicas = 1;
        private String kafkaTopicName = null;
        public int retentionTime = 3 * 24 * 60 * 60 * 1000;

        public NewTopic toNewTopic() {

            NewTopic topic = new NewTopic(this.kafkaTopicName, this.partitions, this.replicas).configs(
                    Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(getRetentionTime())));
            return topic;
        }

        public int getRetentionTime() {
            return retentionTime;
        }

        public void setRetentionTime(int retentionTime) {
            this.retentionTime = retentionTime;
        }

        public String getName() {
            return this.name;
        }

        public boolean isValid() {
            return this.name != null && this.name.trim().length() > 0;
        }

        public int getPartitions() {
            return partitions;
        }

        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }

        public short getReplicas() {
            return replicas;
        }

        public void setReplicas(short replicas) {
            this.replicas = replicas;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void buildKafkaTopicName(String environment) {
            this.kafkaTopicName = String.format(TOPIC_NAME_TEMPLATE, environment, this.name);
        }
    }
}
