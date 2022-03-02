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
public class OrderProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String ORDER_EXCHANGE_PROP_NAME = "${bi.data-processor.order.exchange-name}";
	public static final String ORDER_REPORT_QUEUE_PROP = "${bi.data-processor.order.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.order.routing-key}";

	private static final String ORDER_INIT_RABBIT_CONSUMERS = "${bi.data-processor.order.initRabbitConsumers}";
	private static final String ORDER_MAX_RABBIT_CONSUMERS = "${bi.data-processor.order.maxRabbitConsumers}";
	
	// Bean names
	public static final String ORDER_RABBIT_LISTENER_CONTAINER_FACTORY = "orderRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "orderEventBindingRealTimeETL";
	private static final String ORDER_REPORT_QUEUE_BEAN_NAME = "orderReportQueue";
	private static final String ORDER_DIRECT_EXCHANGE_BEAN_NAME = "orderDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String orderDirectExchangeName;

	public OrderProcessorRabbitConfiguration(
			@Value(OrderProcessorRabbitConfiguration.ORDER_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(OrderProcessorRabbitConfiguration.ORDER_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(OrderProcessorRabbitConfiguration.ORDER_EXCHANGE_PROP_NAME) String orderDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.orderDirectExchangeName = orderDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(ORDER_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(OrderProcessorRabbitConfiguration.ORDER_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(OrderProcessorRabbitConfiguration.ORDER_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(OrderProcessorRabbitConfiguration.ORDER_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange orderTopicExchangeName() {
		return createEventExchange(orderDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(OrderProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { OrderProcessorRabbitConfiguration.ORDER_DIRECT_EXCHANGE_BEAN_NAME,
			OrderProcessorRabbitConfiguration.ORDER_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(OrderProcessorRabbitConfiguration.ORDER_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(OrderProcessorRabbitConfiguration.ORDER_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
