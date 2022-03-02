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
public class PlacementCustomColumnProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String PLACEMENT_CUSTOM_COLUMN_EXCHANGE_PROP_NAME = "${bi.data-processor.placement_custom_column.exchange-name}";
	public static final String PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_PROP = "${bi.data-processor.placement_custom_column.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.placement_custom_column.routing-key}";

	private static final String PLACEMENT_CUSTOM_COLUMN_INIT_RABBIT_CONSUMERS = "${bi.data-processor.placement_custom_column.initRabbitConsumers}";
	private static final String PLACEMENT_CUSTOM_COLUMN_MAX_RABBIT_CONSUMERS = "${bi.data-processor.placement_custom_column.maxRabbitConsumers}";
	
	// Bean names
	public static final String PLACEMENT_CUSTOM_COLUMN_RABBIT_LISTENER_CONTAINER_FACTORY = "placementCustomColumnRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "placementCustomColumnEventBindingRealTimeETL";
	private static final String PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_BEAN_NAME = "placementCustomColumnReportQueue";
	private static final String PLACEMENT_CUSTOM_COLUMN_DIRECT_EXCHANGE_BEAN_NAME = "placementCustomColumnDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String placementCustomColumnDirectExchangeName;

	public PlacementCustomColumnProcessorRabbitConfiguration(
			@Value(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_EXCHANGE_PROP_NAME) String placementCustomColumnDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.placementCustomColumnDirectExchangeName = placementCustomColumnDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(PLACEMENT_CUSTOM_COLUMN_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange placementCustomColumnTopicExchangeName() {
		return createEventExchange(placementCustomColumnDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(PlacementCustomColumnProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_DIRECT_EXCHANGE_BEAN_NAME,
			PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(PlacementCustomColumnProcessorRabbitConfiguration.PLACEMENT_CUSTOM_COLUMN_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
