package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import com.mediaocean.platform.bi.data.sensor.config.Constants;
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

@Configuration
public class PlacementDailyFlightingProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String PLACEMENT_DAILY_FLIGHTING_EXCHANGE_PROP_NAME = "${bi.data-processor.placementdailyflighting.exchange-name}";
	public static final String PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_PROP = "${bi.data-processor.placementdailyflighting.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.placementdailyflighting.routing-key}";

	private static final String PLACEMENT_DAILY_FLIGHTING_INIT_RABBIT_CONSUMERS = "${bi.data-processor.placementdailyflighting.initRabbitConsumers}";
	private static final String PLACEMENT_DAILY_FLIGHTING_MAX_RABBIT_CONSUMERS = "${bi.data-processor.placementdailyflighting.maxRabbitConsumers}";

	// Bean names
	public static final String PLACEMENT_DAILY_FLIGHTING_RABBIT_LISTENER_CONTAINER_FACTORY = "placementDailyFlightingRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "placementDailyFlightingEventBindingRealTimeETL";
	private static final String PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_BEAN_NAME = "placementDailyFlightingReportQueue";
	private static final String PLACEMENT_DAILY_FLIGHTING_DIRECT_EXCHANGE_BEAN_NAME = "placementDailyFlightingDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String placementDailyFlightingDirectExchangeName;

	public PlacementDailyFlightingProcessorRabbitConfiguration(
			@Value(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_EXCHANGE_PROP_NAME) String placementDailyFlightingDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.placementDailyFlightingDirectExchangeName = placementDailyFlightingDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(PLACEMENT_DAILY_FLIGHTING_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange placementDailyFlightingTopicExchangeName() {
		return createEventExchange(placementDailyFlightingDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(PlacementDailyFlightingProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_DIRECT_EXCHANGE_BEAN_NAME,
			PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(PlacementDailyFlightingProcessorRabbitConfiguration.PLACEMENT_DAILY_FLIGHTING_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
