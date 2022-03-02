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
public class BuyChangeSensorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String BUY_CHANGE_EXCHANGE_PROP_NAME = "${bi.data-sensor.buychangeeventsensor.exchange-name}";
	public static final String BUY_CHANGE_QUEUE_PROP = "${bi.data-sensor.buychangeeventsensor.queue}";

	// Event configuration
	private static final String BUY_CHANGE_ROUING_KEY_PROP = "${bi.data-sensor.buychangeeventsensor.routing-key}";
	private static final String BUY_CHANGE_REALTIME_ROUING_KEY_PROP = "${bi.data-sensor.buychangeeventsensor.realtime-routing-key}";

	private static final String INIT_RABBIT_CONSUMERS = "${bi.data-sensor.buychangeeventsensor.initRabbitConsumers}";
	private static final String MAX_RABBIT_CONSUMERS = "${bi.data-sensor.buychangeeventsensor.maxRabbitConsumers}";

	// Bean names
	public static final String BUY_CHANGE_EVENT_RABBIT_LISTENER_CONTAINER_FACTORY = "buyChangeRabbitListenerContainerFactory";
	private static final String BUY_CHANGE_EXCHANGE_BEAN_NAME = "buyChangeExchange";
	private static final String BUY_CHANGE_QUEUE_NAME = "buyChangeQueue";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String buyChangeExchangeName;

	public BuyChangeSensorRabbitConfiguration(
			@Value(BuyChangeSensorRabbitConfiguration.INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(BuyChangeSensorRabbitConfiguration.MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_PROP_NAME) String buyChangeExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.buyChangeExchangeName = buyChangeExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(BUY_CHANGE_EVENT_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_NAME)
	Queue buyChangeEventReportQueue(@Value(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_BEAN_NAME)
	DirectExchange buyChangeExchangeName() {
		return createEventExchange(buyChangeExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */

	@Bean
	@DependsOn(value = { BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_BEAN_NAME,
			BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_NAME })
	Binding buyChangeBinding(@Qualifier(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_NAME) Queue queue,
			@Qualifier(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(BUY_CHANGE_ROUING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}
	
	@Bean
	@DependsOn(value = { BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_BEAN_NAME,
			BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_NAME })
	Binding buyChangeRealTimeBinding(@Qualifier(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_QUEUE_NAME) Queue queue,
			@Qualifier(BuyChangeSensorRabbitConfiguration.BUY_CHANGE_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(BUY_CHANGE_REALTIME_ROUING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	
}
