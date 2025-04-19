package ru.nsu.leontev.service.broker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.response.CrackHashWorkerResponse;

@Service
public class ResultSenderService {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String key;

    public ResultSenderService(RabbitTemplate rabbitTemplate,
                               @Value("${rabbit.result.exchange}") String exchange,
                               @Value("${rabbit.result.key}") String key) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.key = key;
    }

    public void publish(CrackHashWorkerResponse crackHashWorkerResponse) {
        rabbitTemplate.convertAndSend(exchange, key, crackHashWorkerResponse);
    }
}
