package com.marcio.ordernotification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ORDERS = "orders.exchange";
    public static final String EXCHANGE_ORDERS_DLX = "orders.dlx";
    
    public static final String QUEUE_STATUS_CHANGED = "orders.status-changed";
    public static final String QUEUE_STATUS_CHANGED_DLQ = "orders.status-changed.dlq";
    
    public static final String ROUTING_KEY_STATUS_CHANGED = "order.status.changed";

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(EXCHANGE_ORDERS);
    }

    @Bean
    public DirectExchange ordersDlx() {
        return new DirectExchange(EXCHANGE_ORDERS_DLX);
    }

    @Bean
    public Queue statusChangedQueue() {
        return QueueBuilder.durable(QUEUE_STATUS_CHANGED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_ORDERS_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_STATUS_CHANGED)
                .build();
    }

    @Bean
    public Queue statusChangedDlq() {
        return QueueBuilder.durable(QUEUE_STATUS_CHANGED_DLQ).build();
    }

    @Bean
    public Binding statusChangedBinding(Queue statusChangedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(statusChangedQueue).to(ordersExchange).with(ROUTING_KEY_STATUS_CHANGED);
    }

    @Bean
    public Binding statusChangedDlqBinding(Queue statusChangedDlq, DirectExchange ordersDlx) {
        return BindingBuilder.bind(statusChangedDlq).to(ordersDlx).with(ROUTING_KEY_STATUS_CHANGED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        
        return converter;
    }
}
