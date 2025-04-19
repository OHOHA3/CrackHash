package ru.nsu.leontev.service.broker;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.response.CrackHashWorkerResponse;
import ru.nsu.leontev.service.CrackManagerService;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultReceiverService {
    private final CrackManagerService crackManagerService;

    @RabbitListener(queues = "${rabbit.result.queue}", ackMode = "MANUAL")
    public void receive(CrackHashWorkerResponse response, Channel channel, Message message) throws IOException {
        try {
            String requestId = response.getRequestId();
            int partNumber = response.getPartNumber();
            long combChecked = response.getCombChecked();
            List<String> words = response.getAnswers().getWords();

            log.info("Received result from worker. Request ID: {}, Part: {}, Checked: {}, Words: {}",
                    requestId, partNumber, combChecked, words);

            crackManagerService.handleTaskResult(requestId, partNumber, combChecked, words);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            log.error("Failed with body {}", new String(message.getBody()), e);
        }
    }
}
