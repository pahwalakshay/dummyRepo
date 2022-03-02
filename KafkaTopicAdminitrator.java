package com.mediaocean.platform.bi.data.sensor.config.kafka;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.mediaocean.platform.bi.data.sensor.config.Constants;
import com.mediaocean.platform.bi.data.sensor.config.kafka.KafkaTopicConfiguration.TopicDetails;

@Configuration
public class KafkaTopicAdminitrator {
	private static final String KAFKA_TOPIC_BEANNAME_PREFIX = "kafka_topic_";
	private final KafkaTopicConfiguration kafkaTopicConfig;
	private final GenericWebApplicationContext context;
	private final String environment;

	@Inject
	public KafkaTopicAdminitrator(KafkaTopicConfiguration kafkaTopicConfig, GenericWebApplicationContext context, @Value(Constants.PRISMA_ENVIRONMENT_NAME) String profile) {
		this.kafkaTopicConfig = kafkaTopicConfig;
		this.context = context;
		this.environment=profile;
	}
	
	@PostConstruct
	public void createTopics() {
		kafkaTopicConfig.getTopics().forEach(this::registerTopicBean);
	}

	private void registerTopicBean(TopicDetails t) {
		t.buildKafkaTopicName(this.environment);
		context.registerBean(KafkaTopicAdminitrator.KAFKA_TOPIC_BEANNAME_PREFIX+environment+t.getName().toLowerCase(), NewTopic.class, t::toNewTopic);
	}
}
