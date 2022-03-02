package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import com.mediaocean.platform.bi.data.sensor.config.Constants;

import org.springframework.amqp.core.*;
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
public class PlacementPollingUpdateRabbitConfiguration extends GenericProcessorRabbitConfiguration {


    private static final String PLACEMENT_POLLING_EXCHANGE_PROP_NAME = "${bi.polling.placement.exchange-name}";
    public static final String PLACEMENT_POLLING_QUEUE_PROP = "${bi.polling.placement.queue}";
    private static final String PLACEMENT_POLLING_ROUTING_KEY_PROP = "${bi.polling.placement.routing-key}";

    private static final String PLACEMENT_POLLING_DLQ = "${bi.polling.placement.dlq}";
    private static final String PLACEMENT_POLLING_DLQ_EXCHANGE_PROP_NAME = "${bi.polling.placement.exchange-name-dlq}";

    private static final String PLACEMENT_POLLING_INIT_RABBIT_CONSUMERS = "${bi.polling.placement.initRabbitConsumers}";
    private static final String PLACEMENT_POLLING_MAX_RABBIT_CONSUMERS = "${bi.polling.placement.maxRabbitConsumers}";

    // Bean names
    public static final String PLACEMENT_POLLING_RABBIT_LISTENER_CONTAINER_FACTORY = "placementPollingRabbitListenerContainerFactory";

    private static final String PLACEMENT_POLLING_EXCHANGE_BINDING_BEAN_NAME = "placementPollingBindingRealTimeETL";
    private static final String PLACEMENT_POLLING_QUEUE_BEAN_NAME = "placementPollingQueue";
    private static final String PLACEMENT_POLLING_DIRECT_EXCHANGE_BEAN_NAME = "placementPollingDirectExchange";


    private static final String PLACEMENT_POLLING_DLQ_EXCHANGE_BINDING_BEAN_NAME = "placementPollingEventBindingDLQ";
    private static final String PLACEMENT_POLLING_DLQ_BEAN_NAME = "placementPollingDLQ";
    private static final String PLACEMENT_POLLING_DLQ_EXCHANGE_BEAN_NAME = "placementPollingDLQExchange";

    // State
    private int initRabbitConsumers;
    private int maxRabbitConsumers;
    private int prefetchCount;
    private String placementPollingDirectExchangeName;
    private String placementPollingDLQExchangeName;

    public PlacementPollingUpdateRabbitConfiguration(
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_INIT_RABBIT_CONSUMERS) int initRabbitConsumers,
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_MAX_RABBIT_CONSUMERS) int maxRabbitConsumers,
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_EXCHANGE_PROP_NAME) String placementPollingDirectExchangeName,
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_PROP_NAME) String placementPollingDLQExchangeName,
            @Value(Constants.BI_RABBITMQ_DEFAULT_LISTENER_PREFETCH) int prefetchCount) {

        this.initRabbitConsumers = initRabbitConsumers;
        this.maxRabbitConsumers = maxRabbitConsumers;
        this.placementPollingDirectExchangeName = placementPollingDirectExchangeName;
        this.placementPollingDLQExchangeName = placementPollingDLQExchangeName;
        this.prefetchCount = prefetchCount;

    }

    @Bean(PLACEMENT_POLLING_RABBIT_LISTENER_CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        return createConnectionFactory(connectionFactory, initRabbitConsumers, maxRabbitConsumers, prefetchCount);
    }

    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_QUEUE_BEAN_NAME)
    Queue placementPollingQueue(@Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_QUEUE_PROP) String queueName,
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ) String dlqName,
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_PROP_NAME) String exchangeDlq) {
        return QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", exchangeDlq)
                .withArgument("x-ha-policy", "all").withArgument("x-dead-letter-routing-key", dlqName).build();
    }

    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_BEAN_NAME)
    Queue placementPollingDLQ(
            @Value(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ) String placementPollingDLQueue) {
        return QueueBuilder.durable(placementPollingDLQueue).withArgument("x-ha-policy", "all").build();
    }

    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DIRECT_EXCHANGE_BEAN_NAME)
    TopicExchange placementPollingExchangeName() {
        return  new TopicExchange(placementPollingDirectExchangeName, true, false);
    }

    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_BEAN_NAME)
    TopicExchange placementPollingDLQExchange() {
        return  new TopicExchange(placementPollingDLQExchangeName, true, false);
    }

    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_EXCHANGE_BINDING_BEAN_NAME)
    @DependsOn(value = { PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DIRECT_EXCHANGE_BEAN_NAME,
            PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_QUEUE_BEAN_NAME })
    Binding placementPollingBindingRealtimeETL(
            @Qualifier(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_QUEUE_BEAN_NAME) Queue queue,
            @Qualifier(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DIRECT_EXCHANGE_BEAN_NAME) TopicExchange exchange,
            @Value(PLACEMENT_POLLING_ROUTING_KEY_PROP) String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /**
     * Binding dead letter queue
     */
    @Bean(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_BINDING_BEAN_NAME)
    @DependsOn(value = { PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_BEAN_NAME,
            PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_BEAN_NAME })
    Binding auditEventExchangeEventBindingDLQ(
            @Qualifier(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_BEAN_NAME) Queue queue,
            @Qualifier(PlacementPollingUpdateRabbitConfiguration.PLACEMENT_POLLING_DLQ_EXCHANGE_BEAN_NAME) TopicExchange exchange,
            @Value(PLACEMENT_POLLING_ROUTING_KEY_PROP) String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
