package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

@Configuration
public class PlacementProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String PLACEMENT_EXCHANGE_PROP_NAME = "${bi.data-processor.placement.exchange-name}";
	public static final String PLACEMENT_REPORT_QUEUE_PROP = "${bi.data-processor.placement.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.placement.routing-key}";

	private static final String PLACEMENT_INIT_RABBIT_CONSUMERS = "${bi.data-processor.placement.initRabbitConsumers}";
	private static final String PLACEMENT_MAX_RABBIT_CONSUMERS = "${bi.data-processor.placement.maxRabbitConsumers}";
	
	// Bean names
	public static final String PLACEMENT_RABBIT_LISTENER_CONTAINER_FACTORY = "placementRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "placementEventBindingRealTimeETL";
	private static final String PLACEMENT_REPORT_QUEUE_BEAN_NAME = "placementReportQueue";
	private static final String PLACEMENT_DIRECT_EXCHANGE_BEAN_NAME = "placementDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String placementDirectExchangeName;

	public PlacementProcessorRabbitConfiguration(
			@Value(PlacementProcessorRabbitConfiguration.PLACEMENT_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(PlacementProcessorRabbitConfiguration.PLACEMENT_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(PlacementProcessorRabbitConfiguration.PLACEMENT_EXCHANGE_PROP_NAME) String placementDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.placementDirectExchangeName = placementDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(PLACEMENT_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(PlacementProcessorRabbitConfiguration.PLACEMENT_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(PlacementProcessorRabbitConfiguration.PLACEMENT_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(PlacementProcessorRabbitConfiguration.PLACEMENT_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange placementTopicExchangeName() {
		return createEventExchange(placementDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(PlacementProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { PlacementProcessorRabbitConfiguration.PLACEMENT_DIRECT_EXCHANGE_BEAN_NAME,
			PlacementProcessorRabbitConfiguration.PLACEMENT_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(PlacementProcessorRabbitConfiguration.PLACEMENT_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(PlacementProcessorRabbitConfiguration.PLACEMENT_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
