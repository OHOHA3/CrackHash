package ru.nsu.leontev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.rest.exception.TaskNotFoundException;
import ru.nsu.leontev.rest.userinteraction.model.CompleteResponse;
import ru.nsu.leontev.rest.userinteraction.model.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class CrackManagerService {
    private static final ConcurrentHashMap<UUID, TaskInfo> TASKS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<TaskInfo> WAITING_TASKS = new ConcurrentLinkedQueue<>();
    private final RestTemplate restTemplate;
    private final EndpointSolver endpointSolver;
    private final Environment env;
    private final int numWorkers;
    private final String alphabetLetters;

    @Autowired
    public CrackManagerService(RestTemplate restTemplate, EndpointSolver endpointSolver, Environment env) {
        this.restTemplate = restTemplate;
        this.endpointSolver = endpointSolver;
        this.env = env;
        numWorkers = env.getProperty("worker.num", Integer.class);
        alphabetLetters = env.getProperty("alphabet", String.class);
    }

    public String handleTask(String hash, int maxLength) {
        UUID taskId = UUID.randomUUID();
        TaskInfo taskInfo = new TaskInfo(taskId.toString(), hash, maxLength, numWorkers, alphabetLetters);
        if (isFullTaskQueue()) {
            WAITING_TASKS.add(taskInfo);
        } else {
            startTask(taskInfo);
        }
        return taskId.toString();
    }

    private void startTask(TaskInfo taskInfo) {
        String taskId = taskInfo.getId();
        taskInfo.setCreatedAt(LocalDateTime.now());
        TASKS.put(UUID.fromString(taskId), taskInfo);

        for (int i = 0; i < numWorkers; i++) {
            CrackHashManagerRequest request = prepareRequest(taskId, taskInfo.getHash(), taskInfo.getMaxLength(), i);
            String workerUrl = endpointSolver.getTaskEndpoint(i);
            restTemplate.postForObject(workerUrl, request, String.class);
        }
    }

    private CrackHashManagerRequest prepareRequest(String taskId, String hash, int maxLength, int partNumber) {
        CrackHashManagerRequest request = new CrackHashManagerRequest();
        request.setRequestId(taskId);
        request.setPartNumber(partNumber);
        request.setPartCount(numWorkers);
        request.setHash(hash);
        request.setMaxLength(maxLength);
        CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();
        alphabet.getSymbols().addAll(List.of(alphabetLetters.split("")));
        request.setAlphabet(alphabet);
        return request;
    }

    public void handleTaskResult(String requestId, int partNumber, long combChecked, List<String> words) {
        TaskInfo taskInfo = getTaskInfo(requestId);
        taskInfo.getData().addAll(words);
        taskInfo.setWordsComplete(combChecked, partNumber);
        taskInfo.setStatus(Status.READY, partNumber);
        if (!isFullTaskQueue() && !WAITING_TASKS.isEmpty()) {
            startTask(WAITING_TASKS.poll());
        }
    }

    public TaskInfo getTaskStatus(String taskId) {
        TaskInfo taskInfo = getTaskInfo(taskId);
        for (int i = 0; i < numWorkers; i++) {
            if (taskInfo.getPartStatus(i) == Status.IN_PROGRESS) {
                String workerUrl = endpointSolver.getStatusEndpoint(i, taskId);
                CompleteResponse completeResponse = restTemplate.getForObject(workerUrl, CompleteResponse.class);
                taskInfo.setWordsComplete(completeResponse.checkedCombs(), i);
                taskInfo.getData().addAll(completeResponse.matches());
            }
        }
        return taskInfo;
    }
    public TaskInfo getTaskInfo(String taskId) {
        UUID uuid = UUID.fromString(taskId);
        TaskInfo taskInfo = TASKS.get(uuid);
        if (taskInfo == null) {
            throw new TaskNotFoundException("Task with id " + taskId + " not found");
        }
        return taskInfo;
    }

    public List<UUID> getTaskIds() {
        return TASKS.keySet().stream().toList();
    }

    private boolean isFullTaskQueue() {
        int maxTasks = env.getProperty("task.max", Integer.class);
        return TASKS.values().stream()
                .filter(task -> task.getStatus() == Status.IN_PROGRESS).count() == maxTasks;
    }

    @Scheduled(fixedRate = 5000)
    public void checkTaskTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        for (TaskInfo task : TASKS.values()) {
            if (ChronoUnit.SECONDS.between(task.getCreatedAt(), now) > 15) {
                for (int i = 0; i < numWorkers; i++) {
                    if (task.getPartStatus(i) == Status.IN_PROGRESS) {
                        task.setStatus(Status.ERROR, i);
                    }
                }
            }
        }
    }
}
