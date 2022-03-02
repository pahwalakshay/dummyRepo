package com.mediaocean.platform.bi.data.sensor.config.rabbit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public class GenericProcessorRabbitConfiguration {

	public int initRabbitConsumers;
	public int maxRabbitConsumers;
	public int prefetchCount;
	public String exchangeName;

	public SimpleRabbitListenerContainerFactory createConnectionFactory(ConnectionFactory connectionFactory,

			int initRabbitConsumers, int maxRabbitConsumers, int prefetchCount) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrentConsumers(initRabbitConsumers);
		factory.setMaxConcurrentConsumers(maxRabbitConsumers);
		factory.setPrefetchCount(prefetchCount);
		return factory;
	}

	Queue createEventQueue(String queueName) {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		return new Queue(queueName, true, false, false, map);
	}

	DirectExchange createEventExchange(String exchangeName) {
		return new DirectExchange(exchangeName, true, false);
	}

	Binding createQueueBinding(Queue queue, DirectExchange exchange, String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);

	}

}
