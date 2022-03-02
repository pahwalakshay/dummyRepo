package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import com.mediaocean.platform.bi.data.sensor.config.Constants;
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
import org.springframework.context.annotation.Profile;

@Profile("background")
@Configuration
public class IPAAuthEventSensorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties
	private static final String IPA_SENSOR_EXCHANGE_PROP = "${bi.data-sensor.ipa-auth-event-sensor.exchange-name}";
	public static final String IPA_SENSOR_QUEUE_PROP = "${bi.data-sensor.ipa-auth-event-sensor.queue}";
	private static final String IPA_SENSOR_DLQ_EXCHANGE_PROP = "${bi.data-sensor.ipa-auth-event-sensor.exchange-name-dlq}";
	private static final String IPA_SENSOR_DLQ_QUEUE_PROP = "${bi.data-sensor.ipa-auth-event-sensor.dlq}";

	// Event configuration
	private static final String ROUTING_KEY_PROP = "${bi.data-sensor.ipa-auth-event-sensor.routing-key}";
	private static final String REALTIME_ETL_ROUTING_KEY_PROP = "${bi.data-sensor.ipa-auth-event-sensor.realtime-routing-key}";

	private static final String INIT_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.ipa-auth-event-sensor.init-rabbit-consumers}";
	private static final String MAX_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.ipa-auth-event-sensor.max-rabbit-consumers}";

	// Bean names
	public static final String IPA_AUTH_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY = "ipaSensorRabbitListenerContainerFactory";
	private static final String IPA_SENSOR_REALTIME_BINDING_BEAN = "ipaAuthEventBindingRealTimeETL";
	private static final String IPA_SENSOR_DLQ_BINDING_BEAN_NAME = "ipaAuthEventBindingDLQ";

	private static final String IPA_SENSOR_QUEUE_BEAN_NAME = "ipaSensorQueue";
	private static final String IPA_SENSOR_EXCHANGE_BEAN_NAME = "ipaSensorExchange";
	private static final String IPA_SENSOR_DLQ_QUEUE_BEAN_NAME = "ipaSensorQueueDLQ";
	private static final String IPA_SENSOR_DLQ_EXCHANGE_BEAN_NAME = "ipaSensorExchangeDLQ";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String ipaSensorDirectExchangeName;
	private String ipaSensorDLQExchange;

	public IPAAuthEventSensorRabbitConfiguration(
			@Value(IPAAuthEventSensorRabbitConfiguration.INIT_RABBIT_CONSUMERS_PROP) int initRabbitConsumers,
			@Value(IPAAuthEventSensorRabbitConfiguration.MAX_RABBIT_CONSUMERS_PROP) int maxRabbitConsumers,
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_PROP) String ipaSensorDirectExchangeName,
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_EXCHANGE_PROP) String directExchangeNameDLQ,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {
		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.ipaSensorDirectExchangeName = ipaSensorDirectExchangeName;
		this.ipaSensorDLQExchange = directExchangeNameDLQ;
		this.prefetchCount = prefetchCount;
	}

	@Bean(IPA_AUTH_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);
	}

	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_BEAN_NAME)
	Queue ipaAuthEventReportQueue(
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_PROP) String queueName,
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_QUEUE_PROP) String dlqName,
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_EXCHANGE_PROP) String exchangeDlq) {
		return QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", exchangeDlq)
				.withArgument("x-ha-policy", "all").withArgument("x-dead-letter-routing-key", dlqName).build();
	}

	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_QUEUE_BEAN_NAME)
	Queue ipaSensorReportQueueDLQ(
			@Value(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_QUEUE_PROP) String ipaSensorReportDLQueue) {
		return createEventQueue(ipaSensorReportDLQueue);
	}

	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_BEAN_NAME)
	DirectExchange ipaSensorExchangeName() {
		return createEventExchange(ipaSensorDirectExchangeName);
	}

	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_EXCHANGE_BEAN_NAME)
	DirectExchange ipaSensorDLQExchange() {
		return createEventExchange(ipaSensorDLQExchange);
	}

	@Bean
	@DependsOn(value = { IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_BEAN_NAME,
			IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_BEAN_NAME})
	Binding ipaSensorBinding(
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_REALTIME_BINDING_BEAN)
	@DependsOn(value = { IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_BEAN_NAME,
			IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_BEAN_NAME})
	Binding ipaAuthEventBindingRealtimeETL(
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(IPAAuthEventSensorRabbitConfiguration.REALTIME_ETL_ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Binding dead letter queue
	 */
	@Bean(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_BINDING_BEAN_NAME)
	@DependsOn(value = { IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_EXCHANGE_BEAN_NAME,
			IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_QUEUE_BEAN_NAME})
	Binding ipaAuthEventExchangeEventBindingDLQ(
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(IPAAuthEventSensorRabbitConfiguration.IPA_SENSOR_DLQ_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);
	}

}
