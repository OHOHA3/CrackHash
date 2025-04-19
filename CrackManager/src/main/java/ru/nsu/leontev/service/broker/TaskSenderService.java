package ru.nsu.leontev.service.broker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.request.CrackHashManagerRequest;

@Service
public class TaskSenderService {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String key;

    public TaskSenderService(RabbitTemplate rabbitTemplate,
                             @Value("${rabbit.task.exchange}") String exchange,
                             @Value("${rabbit.task.key}") String key) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.key = key;
    }

    public void publish(CrackHashManagerRequest crackHashManagerRequest) {
        rabbitTemplate.convertAndSend(exchange, key, crackHashManagerRequest);
    }
}
