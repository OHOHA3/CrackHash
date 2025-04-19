package ru.nsu.leontev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.leontev.AppContext;
import ru.nsu.leontev.db.entity.TaskInfo;
import ru.nsu.leontev.model.CompleteResponse;
import ru.nsu.leontev.model.CrackStatusResponse;
import ru.nsu.leontev.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrackManagerService {
    private static final double FULLY_COMPLETE = 1.0;
    private final TaskInfoService taskInfoService;
    private final WorkerService workerService;
    private final AppContext appContext;

    public String handleTask(String hash, int maxLength) {
        String taskId = UUID.randomUUID().toString();
        TaskInfo taskInfo = new TaskInfo(taskId, hash, maxLength, appContext.getNumWorkers(),
                appContext.getAlphabet());
        if (taskInfoService.canStartTask()) {
            startTask(taskInfo);
        } else {
            postponeTask(taskInfo);
        }
        return taskId;
    }

    private void postponeTask(TaskInfo taskInfo) {
        taskInfo.setStatus(Status.WAITING);
        taskInfoService.save(taskInfo);
    }

    private void startTask(TaskInfo taskInfo) {
        taskInfo.setStatus(Status.IN_PROGRESS);
        taskInfo.setStartedAt(LocalDateTime.now());
        taskInfoService.save(taskInfo);
        try {
            workerService.sendTaskRequest(taskInfo);
        } catch (AmqpException e) {
            log.error("rabbit dead");
            postponeTask(taskInfo);
        }
    }

    @Transactional
    public void handleTaskResult(String requestId, int partNumber, long combChecked, List<String> words) {
        TaskInfo taskInfo = taskInfoService.findById(requestId);
        taskInfo.getData().addAll(words);
        taskInfo.getWordsComplete()[partNumber] = combChecked;
        System.out.println(taskInfo.getComplete());
        if (taskInfo.getComplete() >= 1d) {
            taskInfo.setStatus(Status.READY);
        }
        taskInfoService.save(taskInfo);
        if (taskInfoService.canStartTask()) {
            taskInfoService.findFirstByStatus(Status.WAITING).ifPresent(this::startTask);
        }
    }

    public CrackStatusResponse getTaskStatus(String requestId) {
        TaskInfo taskInfo = taskInfoService.findById(requestId);
        double complete;
        if (taskInfo.getStatus() == Status.READY) {
            complete = FULLY_COMPLETE;
        } else {
            CompleteResponse completeResponse = workerService.getTaskStatus(requestId);
            long allCombs = (long) (Math.pow(taskInfo.getAlphabet().length(), taskInfo.getMaxLength() + 1)
                    - taskInfo.getAlphabet().length()) / (taskInfo.getAlphabet().length() - 1);
            complete = completeResponse.checkedCombs() / (double) allCombs;
        }
        return new CrackStatusResponse(
                taskInfo.getHash(),
                taskInfo.getAlphabet(),
                taskInfo.getStatus(),
                complete,
                taskInfo.getData().toArray(new String[0]));
    }
}
