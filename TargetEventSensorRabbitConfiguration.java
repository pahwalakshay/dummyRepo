package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

@Configuration
public class TargetEventSensorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties
	private static final String TARGET_SENSOR_EXCHANGE_PROP = "${bi.data-sensor.targeteventsensor.exchange-name}";
	public static final String TARGET_SENSOR_QUEUE_PROP = "${bi.data-sensor.targeteventsensor.queue}";
	private static final String TARGET_SENSOR_DLQ_EXCHANGE_PROP = "${bi.data-sensor.targeteventsensor.exchange-name-dlq}";
	private static final String TARGET_SENSOR_DLQ_QUEUE_PROP = "${bi.data-sensor.targeteventsensor.dlq}";

	// Event configuration
	private static final String ROUTING_KEY_PROP = "${bi.data-sensor.targeteventsensor.routing-key}";
	private static final String REALTIME_ETL_ROUTING_KEY_PROP = "${bi.data-sensor.targeteventsensor.realtime-routing-key}";

	private static final String INIT_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.targeteventsensor.initRabbitConsumers}";
	private static final String MAX_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.targeteventsensor.maxRabbitConsumers}";

	// Bean names
	public static final String TARGET_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY = "targetSensorRabbitListenerContainerFactory";
	private static final String TARGET_SENSOR_REALTIME_BINDING_BEAN = "targetSensorEventBindingRealTimeETL";
	private static final String TARGET_SENSOR_DLQ_BINDING_BEAN_NAME = "targeteventsensorEventBindingDLQ";

	private static final String TARGET_SENSOR_QUEUE_BEAN_NAME = "targetSensorQueue";
	private static final String TARGET_SENSOR_EXCHANGE_BEAN_NAME = "targetSensorExchange";
	private static final String TARGET_SENSOR_DLQ_QUEUE_BEAN_NAME = "targetSensorQueueDLQ";
	private static final String TARGET_SENSOR_DLQ_EXCHANGE_BEAN_NAME = "targetSensorExchangeDLQ";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String targetsensorDirectExchangeName;
	private String targetSensorDLQExchange;

	public TargetEventSensorRabbitConfiguration(
			@Value(TargetEventSensorRabbitConfiguration.INIT_RABBIT_CONSUMERS_PROP) int initRabbitConsumers,
			@Value(TargetEventSensorRabbitConfiguration.MAX_RABBIT_CONSUMERS_PROP) int maxRabbitConsumers,
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_PROP) String targetsensorDirectExchangeName,
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_EXCHANGE_PROP) String directExchangeNameDLQ,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {
		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.targetsensorDirectExchangeName = targetsensorDirectExchangeName;
		this.targetSensorDLQExchange = directExchangeNameDLQ;
		this.prefetchCount = prefetchCount;
	}

	@Bean(TARGET_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);
	}

	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_PROP) String queueName,
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_QUEUE_PROP) String dlqName,
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_EXCHANGE_PROP) String exchangeDlq) {
		return QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", exchangeDlq)
				.withArgument("x-ha-policy", "all").withArgument("x-dead-letter-routing-key", dlqName).build();
	}

	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_QUEUE_BEAN_NAME)
	Queue eventsensorReportQueueDLQ(
			@Value(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_QUEUE_PROP) String eventsensorReportDLQueue) {
		return createEventQueue(eventsensorReportDLQueue);
	}

	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_BEAN_NAME)
	DirectExchange inovicesensorExchangeName() {
		return createEventExchange(targetsensorDirectExchangeName);
	}

	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_EXCHANGE_BEAN_NAME)
	DirectExchange targetsensorDLQExchange() {
		return createEventExchange(targetSensorDLQExchange);
	}

	@Bean
	@DependsOn(value = { TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_BEAN_NAME,
			TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_BEAN_NAME })
	Binding targetSensorBinding(
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_REALTIME_BINDING_BEAN)
	@DependsOn(value = { TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_BEAN_NAME,
			TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_BEAN_NAME })
	Binding targetExchangeEventBindingRealtimeETL(
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(TargetEventSensorRabbitConfiguration.REALTIME_ETL_ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Binding dead letter queue
	 */
	@Bean(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_BINDING_BEAN_NAME)
	@DependsOn(value = { TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_EXCHANGE_BEAN_NAME,
			TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingDLQ(
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(TargetEventSensorRabbitConfiguration.TARGET_SENSOR_DLQ_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);
	}

}
