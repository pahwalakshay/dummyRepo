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
public class InvoiceProcessorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties

	private static final String INVOICE_EXCHANGE_PROP_NAME = "${bi.data-processor.invoice.exchange-name}";
	public static final String INVOICE_PROCESSOR_QUEUE_PROP = "${bi.data-processor.invoice.queue}";

	// Event configuration
	private static final String ROUING_KEY_EVENT_REALTIME_ETL = "${bi.data-processor.invoice.routing-key}";

	private static final String INVOICE_INIT_RABBIT_CONSUMERS = "${bi.data-processor.invoice.initRabbitConsumers}";
	private static final String INVOICE_MAX_RABBIT_CONSUMERS = "${bi.data-processor.invoice.maxRabbitConsumers}";
	
	// Bean names
	public static final String INVOICE_RABBIT_LISTENER_CONTAINER_FACTORY = "invoiceProcessorRabbitListenerContainerFactory";
	private static final String INVOICE_PROCESSOR_REALTIME_BINDING_BEAN = "invoiceProcessorEventBindingRealTimeETL";
	private static final String INVOICE_PROCESSOR_QUEUE_BEAN_NAME = "invoiceProcessorQueue";
	private static final String INVOICE_PROCESSOR_EXCHANGE_BEAN_NAME = "invoiceProcessorExchange";


	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String invoiceDirectExchangeName;

	public InvoiceProcessorRabbitConfiguration(
			@Value(InvoiceProcessorRabbitConfiguration.INVOICE_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
			@Value(InvoiceProcessorRabbitConfiguration.INVOICE_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
			@Value(InvoiceProcessorRabbitConfiguration.INVOICE_EXCHANGE_PROP_NAME) String invoiceDirectExchangeName,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.invoiceDirectExchangeName = invoiceDirectExchangeName;
		this.prefetchCount = prefetchCount;

	}

	@Bean(INVOICE_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);

	}

	@Bean(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(@Value(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_QUEUE_PROP) String queueName) {
		return createEventQueue(queueName);
	}

	@Bean(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_EXCHANGE_BEAN_NAME)
	DirectExchange invoiceTopicExchangeName() {
		return createEventExchange(invoiceDirectExchangeName);
	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_REALTIME_BINDING_BEAN)
	@DependsOn(value = { InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_EXCHANGE_BEAN_NAME,
			InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingRealtimeETL(
			@Qualifier(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(InvoiceProcessorRabbitConfiguration.INVOICE_PROCESSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUING_KEY_EVENT_REALTIME_ETL) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

}
