package ru.nsu.leontev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.leontev.AppContext;
import ru.nsu.leontev.db.entity.TaskInfo;
import ru.nsu.leontev.model.CompleteResponse;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.service.broker.TaskSenderService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService {
    private final RestTemplate restTemplate;
    private final TaskSenderService taskSenderService;
    private final AppContext appContext;

    public void sendTaskRequest(TaskInfo taskInfo) {
        for (int i = 0; i < appContext.getNumWorkers(); i++) {
            log.info("send task {}-{}", taskInfo.getId(), i);
            CrackHashManagerRequest request = prepareRequest(taskInfo.getId(), taskInfo.getHash(), taskInfo.getMaxLength(), i);
            taskSenderService.publish(request);
        }
    }

    private CrackHashManagerRequest prepareRequest(String taskId, String hash, int maxLength, int partNumber) {
        CrackHashManagerRequest request = new CrackHashManagerRequest();
        request.setRequestId(taskId);
        request.setPartNumber(partNumber);
        request.setPartCount(appContext.getNumWorkers());
        request.setHash(hash);
        request.setMaxLength(maxLength);
        CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();
        alphabet.getSymbols().addAll(List.of(appContext.getAlphabet().split("")));
        request.setAlphabet(alphabet);
        return request;
    }


    public CompleteResponse getTaskStatus(String taskId) {
        long complete = 0;
        Set<String> words = new HashSet<>();
        for (int i = 0; i < appContext.getNumWorkers(); i++) {
            CompleteResponse completeResponse = sendTaskStatusRequest(taskId, i);
            complete += completeResponse.checkedCombs();
            words.addAll(completeResponse.matches());
        }
        return new CompleteResponse(complete, words);
    }

    private CompleteResponse sendTaskStatusRequest(String taskId, int part) {
        log.info("send status request {}-{}", taskId, part);
        String workerUrl = appContext.getWorkerUrl(part) + '/' + appContext.getWorkerUrn() + '/' + taskId;
        try {
            return restTemplate.getForObject(workerUrl, CompleteResponse.class);
        } catch (Exception e) {
            log.info("worker not found {}", workerUrl);
            return new CompleteResponse(0, Set.of());
        }
    }
}
