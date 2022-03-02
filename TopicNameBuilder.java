package com.mediaocean.platform.bi.data.sensor.config.kafka;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

@Named
public class TopicNameBuilder {
	private static final String KAFKA_TOPIC_BEANNAME_PREFIX = "kafka_topic_";
	private static final String KAFKA_TOPIC_SEPARATOR = "_";
	private String environmentPreix;

	public TopicNameBuilder(@Value(Constants.PRISMA_ENVIRONMENT_NAME) String environment) {
		this.environmentPreix = KAFKA_TOPIC_BEANNAME_PREFIX + KAFKA_TOPIC_SEPARATOR + environment.toLowerCase()
				+ KAFKA_TOPIC_SEPARATOR;
	}

	public String build(String topic) {
		return environmentPreix + topic.toLowerCase();
	}
}
