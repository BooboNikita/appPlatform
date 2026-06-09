package com.app.event.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.event:app.event.queue}")
    private String eventQueueName;

    @Value("${rabbitmq.exchange.event:app.event.exchange}")
    private String eventExchangeName;

    @Value("${rabbitmq.routing-key.event:app.event.routing.key}")
    private String eventRoutingKey;

    // 事件队列
    @Bean
    public Queue eventQueue() {
        return new Queue(eventQueueName, true);
    }

    // 事件交换机
    @Bean
    public DirectExchange eventExchange() {
        return new DirectExchange(eventExchangeName);
    }

    // 绑定队列到交换机
    @Bean
    public Binding eventBinding() {
        return BindingBuilder.bind(eventQueue())
                .to(eventExchange())
                .with(eventRoutingKey);
    }

    // 消息转换器
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 配置RabbitTemplate
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
