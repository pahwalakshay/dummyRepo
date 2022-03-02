package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

@Configuration
public class AuditEventSensorRabbitConfiguration {

	// Configurable properties
	private static final String EVENT_SENSOR_EXCHANGE_PROP_NAME = "${bi.data-sensor.auditeventsensor.exchange-name}";
	public static final String EVENT_SENSOR_REPORT_QUEUE_PROP = "${bi.data-sensor.auditeventsensor.queue}";
	private static final String EVENT_SENSOR_REPORT_DLQ = "${bi.data-sensor.auditeventsensor.dlq}";
	private static final String EXCHANGE_PROP_NAME_DLQ = "${bi.data-sensor.auditeventsensor.exchange-name-dlq}";

	// Event configuration
	private static final String ROUTING_KEY_EVENT = "${bi.data-sensor.auditeventsensor.routing-key}";
	private static final String ROUTING_KEY_EVENT_REALTIME_ETL = "${bi.data-sensor.auditeventsensor.realtime-routing-key}";

	private static final String EVENT_SENSOR_INIT_RABBIT_CONSUMERS = "${bi.data-sensor.auditeventsensor.initRabbitConsumers}";
	private static final String EVENT_SENSOR_MAX_RABBIT_CONSUMERS = "${bi.data-sensor.auditeventsensor.maxRabbitConsumers}";

	// Bean names
	public static final String EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY = "eventsensorRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "eventsensorEventBindingRealTimeETL";
	private static final String EXCHANGE_BEAN_NAME_DLQ = "eventsensorExchangeDLQ";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_DLQ = "eventsensorEventBindingDLQ";

	private static final String EVENT_SENSOR_REPORT_DLQ_BEAN_NAME = "eventsensorReportQueueDLQ";
	private static final String EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME = "eventsensorReportQueue";
	private static final String EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME = "eventsensorTopicExchange";
	private static final String INVOICE_EXCHANGE_BEAN_NAME = "invoiceExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String eventsensorTopicExchangeName;
	private String topicExchangeNameDLQ;

	public AuditEventSensorRabbitConfiguration(
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_EXCHANGE_PROP_NAME) String eventsensorTopicExchangeName,
			@Value(AuditEventSensorRabbitConfiguration.EXCHANGE_PROP_NAME_DLQ) String topicExchangeNameDLQ,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {
		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.eventsensorTopicExchangeName = eventsensorTopicExchangeName;
		this.topicExchangeNameDLQ = topicExchangeNameDLQ;
		this.prefetchCount = prefetchCount;
	}

	@Bean(EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrentConsumers(initRabbitConsumers);
		factory.setMaxConcurrentConsumers(maxRabbitConsumers);
		factory.setPrefetchCount(this.prefetchCount);
		return factory;
	}

	@Bean(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_PROP) String queueName,
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_DLQ) String dlqName,
			@Value(AuditEventSensorRabbitConfiguration.EXCHANGE_PROP_NAME_DLQ) String exchangeDlq) {
		return QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", exchangeDlq)
				.withArgument("x-ha-policy", "all").withArgument("x-dead-letter-routing-key", dlqName).build();
	}

	@Bean(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_DLQ_BEAN_NAME)
	Queue eventsensorReportQueueDLQ(
			@Value(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_DLQ) String eventsensorReportDLQueue) {
		return QueueBuilder.durable(eventsensorReportDLQueue).withArgument("x-ha-policy", "all").build();
	}

	@Bean(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME)
	TopicExchange eventsensorTopicExchangeName() {
		return new TopicExchange(eventsensorTopicExchangeName, true, false);
	}

	@Bean(AuditEventSensorRabbitConfiguration.EXCHANGE_BEAN_NAME_DLQ)
	TopicExchange eventsensorDLQTopicExchange() {
		return new TopicExchange(topicExchangeNameDLQ, true, false);
	}

	@Bean
	@DependsOn(value = { AuditEventSensorRabbitConfiguration.EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME,
			AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME })
	Binding eventsensorExchangeEventBinding(
			@Qualifier(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME) TopicExchange exchange,
			@Value(ROUTING_KEY_EVENT) String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(AuditEventSensorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { AuditEventSensorRabbitConfiguration.EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME,
			AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_TOPIC_EXCHANGE_BEAN_NAME) TopicExchange exchange,
			@Value(ROUTING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);

	}

	/**
	 * Binding dead letter queue
	 */
	@Bean(AuditEventSensorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_DLQ)
	@DependsOn(value = { AuditEventSensorRabbitConfiguration.EXCHANGE_BEAN_NAME_DLQ,
			AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_DLQ_BEAN_NAME })
	Binding auditEventExchangeEventBindingDLQ(
			@Qualifier(AuditEventSensorRabbitConfiguration.EVENT_SENSOR_REPORT_DLQ_BEAN_NAME) Queue queue,
			@Qualifier(AuditEventSensorRabbitConfiguration.EXCHANGE_BEAN_NAME_DLQ) TopicExchange exchange,
			@Value(ROUTING_KEY_EVENT) String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}
}
