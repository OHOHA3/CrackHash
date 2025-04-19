package ru.nsu.leontev.service.broker;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.service.CrackWorkerService;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class TaskReceiverService {
    private final CrackWorkerService crackWorkerService;

    @RabbitListener(queues = "${rabbit.task.queue}", ackMode = "MANUAL")
    public void receive(CrackHashManagerRequest request, Channel channel, Message message) throws IOException {
        try {
            log.info("Received crack task: requestId={}", request.getRequestId());

            crackWorkerService.handleTask(request);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.debug("Task sent to crackWorkerService for processing. requestId={}", request.getRequestId());
        } catch (IOException e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            log.error("Failed with body {}", new String(message.getBody()), e);
        }
    }
}
