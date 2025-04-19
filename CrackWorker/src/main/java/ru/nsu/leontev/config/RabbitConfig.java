package ru.nsu.leontev.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MarshallingMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@EnableRabbit
@Configuration
public class RabbitConfig {
    @Value("${rabbit.task.exchange}")
    private String taskExchange;

    @Value("${rabbit.task.queue}")
    private String taskQueue;

    @Value("${rabbit.task.key}")
    private String taskKey;

    @Value("${rabbit.result.exchange}")
    private String resultExchange;

    @Value("${rabbit.result.queue}")
    private String resultQueue;

    @Value("${rabbit.result.key}")
    private String resultKey;

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(taskQueue).build();
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(taskExchange, true, false);
    }

    @Bean
    public Binding taskBinding() {
        return BindingBuilder
                .bind(taskQueue())
                .to(taskExchange())
                .with(taskKey);
    }

    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(resultQueue).build();
    }

    @Bean
    public DirectExchange resultExchange() {
        return new DirectExchange(resultExchange, true, false);
    }

    @Bean
    public Binding resultBinding() {
        return BindingBuilder
                .bind(resultQueue())
                .to(resultExchange())
                .with(resultKey);
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("ru.nsu.leontev.request", "ru.nsu.leontev.response");
        return marshaller;
    }

    @Bean
    public MarshallingMessageConverter marshallingMessageConverter(final Jaxb2Marshaller marshaller) {
        return new MarshallingMessageConverter(marshaller);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
                                         final MarshallingMessageConverter messageConverter) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
