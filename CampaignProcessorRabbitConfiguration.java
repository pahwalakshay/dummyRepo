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
public class CampaignProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String CAMPAIGN_EXCHANGE_PROP_NAME = "${bi.data-processor.campaign.exchange-name}";
	public static final String CAMPAIGN_REPORT_QUEUE_PROP = "${bi.data-processor.campaign.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.campaign.routing-key}";

	private static final String CAMPAIGN_INIT_RABBIT_CONSUMERS = "${bi.data-processor.campaign.initRabbitConsumers}";
	private static final String CAMPAIGN_MAX_RABBIT_CONSUMERS = "${bi.data-processor.campaign.maxRabbitConsumers}";
	
	// Bean names
	public static final String CAMPAIGN_RABBIT_LISTENER_CONTAINER_FACTORY = "campaignRabbitListenerContainerFactory";
	private static final String EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL = "campaignEventBindingRealTimeETL";
	private static final String CAMPAIGN_REPORT_QUEUE_BEAN_NAME = "campaignReportQueue";
	private static final String CAMPAIGN_DIRECT_EXCHANGE_BEAN_NAME = "campaignDirectExchange";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String campaignDirectExchangeName;

	public CampaignProcessorRabbitConfiguration(
			@Value(CampaignProcessorRabbitConfiguration.CAMPAIGN_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(CampaignProcessorRabbitConfiguration.CAMPAIGN_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(CampaignProcessorRabbitConfiguration.CAMPAIGN_EXCHANGE_PROP_NAME) String campaignDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.campaignDirectExchangeName = campaignDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(CAMPAIGN_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(CampaignProcessorRabbitConfiguration.CAMPAIGN_REPORT_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(CampaignProcessorRabbitConfiguration.CAMPAIGN_REPORT_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(CampaignProcessorRabbitConfiguration.CAMPAIGN_DIRECT_EXCHANGE_BEAN_NAME)
	DirectExchange campaignTopicExchangeName() {
		return createEventExchange(campaignDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(CampaignProcessorRabbitConfiguration.EXCHANGE_EVENT_BINDING_BEAN_NAME_REALTIME_ETL)
	@DependsOn(value = { CampaignProcessorRabbitConfiguration.CAMPAIGN_DIRECT_EXCHANGE_BEAN_NAME,
			CampaignProcessorRabbitConfiguration.CAMPAIGN_REPORT_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(CampaignProcessorRabbitConfiguration.CAMPAIGN_REPORT_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(CampaignProcessorRabbitConfiguration.CAMPAIGN_DIRECT_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
