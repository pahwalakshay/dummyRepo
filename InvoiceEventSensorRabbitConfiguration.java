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
public class InvoiceEventSensorRabbitConfiguration extends GenericProcessorRabbitConfiguration {

	// Configurable properties
	private static final String INVOICE_SENSOR_EXCHANGE_PROP = "${bi.data-sensor.invoiceeventsensor.exchange-name}";
	public static final String INVOICE_SENSOR_QUEUE_PROP = "${bi.data-sensor.invoiceeventsensor.queue}";
	private static final String INVOICE_SENSOR_DLQ_EXCHANGE_PROP = "${bi.data-sensor.invoiceeventsensor.exchange-name-dlq}";
	private static final String INVOICE_SENSOR_DLQ_QUEUE_PROP = "${bi.data-sensor.invoiceeventsensor.dlq}";

	// Event configuration
	private static final String ROUTING_KEY_PROP = "${bi.data-sensor.invoiceeventsensor.routing-key}";
	private static final String REALTIME_ETL_ROUTING_KEY_PROP = "${bi.data-sensor.invoiceeventsensor.realtime-routing-key}";

	private static final String INIT_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.invoiceeventsensor.initRabbitConsumers}";
	private static final String MAX_RABBIT_CONSUMERS_PROP = "${bi.data-sensor.invoiceeventsensor.maxRabbitConsumers}";

	// Bean names
	public static final String INVOICE_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY = "invoiceSensorRabbitListenerContainerFactory";
	private static final String INVOICE_SENSOR_REALTIME_BINDING_BEAN = "invoiceSensorEventBindingRealTimeETL";
	private static final String INVOCIE_SENSOR_DLQ_BINDING_BEAN_NAME = "invoiceeventsensorEventBindingDLQ";

	private static final String INVOICE_SENSOR_QUEUE_BEAN_NAME = "invoiceSensorQueue";
	private static final String INVOICE_SENSOR_EXCHANGE_BEAN_NAME = "invoiceSensorExchange";
	private static final String INVOICE_SENSOR_DLQ_QUEUE_BEAN_NAME = "invoiceSensorQueueDLQ";
	private static final String INVOICE_SENSOR_DLQ_EXCHANGE_BEAN_NAME = "invoiceSensorExchangeDLQ";

	// State
	private int initRabbitConsumers;
	private int maxRabbitConsumers;
	private int prefetchCount;
	private String invoicesensorDirectExchangeName;
	private String invoiceSensorDLQExchange;

	public InvoiceEventSensorRabbitConfiguration(
			@Value(InvoiceEventSensorRabbitConfiguration.INIT_RABBIT_CONSUMERS_PROP) int initRabbitConsumers,
			@Value(InvoiceEventSensorRabbitConfiguration.MAX_RABBIT_CONSUMERS_PROP) int maxRabbitConsumers,
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_PROP) String invoicesensorDirectExchangeName,
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_EXCHANGE_PROP) String directExchangeNameDLQ,
			@Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {
		this.initRabbitConsumers = initRabbitConsumers;
		this.maxRabbitConsumers = maxRabbitConsumers;
		this.invoicesensorDirectExchangeName = invoicesensorDirectExchangeName;
		this.invoiceSensorDLQExchange = directExchangeNameDLQ;
		this.prefetchCount = prefetchCount;
	}

	@Bean(INVOICE_EVENT_SENSOR_RABBIT_LISTENER_CONTAINER_FACTORY)
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);
	}

	@Bean(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_BEAN_NAME)
	Queue auditEventReportQueue(
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_PROP) String queueName,
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_QUEUE_PROP) String dlqName,
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_EXCHANGE_PROP) String exchangeDlq) {
		return QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", exchangeDlq)
				.withArgument("x-ha-policy", "all").withArgument("x-dead-letter-routing-key", dlqName).build();
	}

	@Bean(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_QUEUE_BEAN_NAME)
	Queue eventsensorReportQueueDLQ(
			@Value(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_QUEUE_PROP) String eventsensorReportDLQueue) {
		return createEventQueue(eventsensorReportDLQueue);
	}

	@Bean(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_BEAN_NAME)
	DirectExchange inovicesensorExchangeName() {
		return createEventExchange(invoicesensorDirectExchangeName);
	}

	@Bean(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_EXCHANGE_BEAN_NAME)
	DirectExchange invoicesensorDLQExchange() {
		return createEventExchange(invoiceSensorDLQExchange);
	}

	@Bean
	@DependsOn(value = { InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_BEAN_NAME,
			InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_BEAN_NAME })
	Binding inoviceSensorBinding(
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Another routing key binding for same queue to publish messages from
	 * bi-data-sensor service.
	 */
	@Bean(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_REALTIME_BINDING_BEAN)
	@DependsOn(value = { InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_BEAN_NAME,
			InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_BEAN_NAME })
	Binding invoiceExchangeEventBindingRealtimeETL(
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(InvoiceEventSensorRabbitConfiguration.REALTIME_ETL_ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);

	}

	/**
	 * Binding dead letter queue
	 */
	@Bean(InvoiceEventSensorRabbitConfiguration.INVOCIE_SENSOR_DLQ_BINDING_BEAN_NAME)
	@DependsOn(value = { InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_EXCHANGE_BEAN_NAME,
			InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_QUEUE_BEAN_NAME })
	Binding auditEventExchangeEventBindingDLQ(
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_QUEUE_BEAN_NAME) Queue queue,
			@Qualifier(InvoiceEventSensorRabbitConfiguration.INVOICE_SENSOR_DLQ_EXCHANGE_BEAN_NAME) DirectExchange exchange,
			@Value(ROUTING_KEY_PROP) String routingKey) {
		return createQueueBinding(queue, exchange, routingKey);
	}

}
