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
import org.springframework.context.annotation.Profile;

@Profile("background")
@Configuration
public class IPAProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String IPA_EXCHANGE_PROP_NAME = "${bi.data-processor.ipa.exchange-name}";
	public static final String IPA_REPORT_QUEUE_PROP = "${bi.data-processor.ipa.queue}";

	// Event configuration
	private static final String ROUTING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.ipa.routing-key}";

	private static final String IPA_INIT_RABBIT_CONSUMERS = "${bi.data-processor.ipa.initRabbitConsumers}";
	private static final String IPA_MAX_RABBIT_CONSUMERS = "${bi.data-processor.ipa.maxRabbitConsumers}";
	
	// Bean names
	public static final String IPA_RABBIT_LISTENER_CONTAINER_FACTORY = "ipaRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "ipaEventBindingRealTimeETL";
	private static final String IPA_REPORT_QUEUE_BEAN_NAME = "ipaReportQueue";
	private static final String IPA_DIRECT_EXCHANGE_BEAN_NAME = "ipaDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String ipaDirectExchangeName;

	public IPAProcessorRabbitConfiguration(
			@Value(IPAProcessorRabbitConfiguration.IPA_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(IPAProcessorRabbitConfiguration.IPA_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(IPAProcessorRabbitConfiguration.IPA_EXCHANGE_PROP_NAME) String ipaDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.ipaDirectExchangeName = ipaDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(IPA_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(IPAProcessorRabbitConfiguration.IPA_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(IPAProcessorRabbitConfiguration.IPA_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(IPAProcessorRabbitConfiguration.IPA_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange ipaTopicExchangeName() {
		return createEventExchange(ipaDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(IPAProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { IPAProcessorRabbitConfiguration.IPA_DIRECT_EXCHANGE_BEAN_NAME,
			IPAProcessorRabbitConfiguration.IPA_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(IPAProcessorRabbitConfiguration.IPA_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(IPAProcessorRabbitConfiguration.IPA_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
