package ru.nsu.leontev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paukov.combinatorics3.Generator;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.response.CrackHashWorkerResponse;
import ru.nsu.leontev.service.broker.ResultSenderService;
import ru.nsu.leontev.service.data.TaskInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrackWorkerService {
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, TaskInfo>> TASKS = new ConcurrentHashMap<>();
    private final ResultSenderService resultSenderService;

    public void handleTask(CrackHashManagerRequest request) {
        UUID requestId = UUID.fromString(request.getRequestId());

        if (TASKS.containsKey(requestId)) {
            if (TASKS.get(requestId).containsKey(request.getPartNumber())) {
                return;
            }
        }
        TASKS.computeIfAbsent(requestId, k -> new ConcurrentHashMap<>())
                .put(request.getPartNumber(), new TaskInfo());
        crackHash(request);
    }


    private void crackHash(CrackHashManagerRequest request) {
        String taskId = request.getRequestId();
        List<String> alphabet = request.getAlphabet().getSymbols();
        int partNumber = request.getPartNumber();
        int partCount = request.getPartCount();
        int maxLength = request.getMaxLength();

        long totalCombs = (long) (Math.pow(alphabet.size(), maxLength + 1) - alphabet.size()) / (alphabet.size() - 1);
        long partSize = totalCombs / partCount;
        long startIndex = partNumber * partSize;
        long endIndex = (partNumber == partCount - 1) ? totalCombs : startIndex + partSize;

        TaskInfo taskInfo = TASKS.get(UUID.fromString(taskId)).get(partNumber);

        for (int len = 1; len <= maxLength; len++) {
            Generator.permutation(alphabet).withRepetitions(len).stream()
                    .skip(startIndex)
                    .limit(endIndex - startIndex)
                    .map(combination -> String.join("", combination))
                    .peek(comb -> taskInfo.combChecked())
                    .filter(candidate -> hash(candidate).equals(request.getHash()))
                    .forEach(taskInfo::addMatch);
        }
        sendResponse(prepareResponse(taskId, partNumber, taskInfo));
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private CrackHashWorkerResponse prepareResponse(String requestId, int partNumber, TaskInfo taskInfo) {
        CrackHashWorkerResponse response = new CrackHashWorkerResponse();
        response.setRequestId(requestId);
        response.setPartNumber(partNumber);
        response.setCombChecked(taskInfo.getCheckedCombs());
        CrackHashWorkerResponse.Answers answers = new CrackHashWorkerResponse.Answers();
        answers.getWords().addAll(taskInfo.getMatches());
        response.setAnswers(answers);
        return response;
    }

    public TaskInfo getTaskInfo(String taskId) {
        if (!TASKS.containsKey(UUID.fromString(taskId))) {
            return new TaskInfo();
        }
        return TASKS.get(UUID.fromString(taskId)).values().stream().reduce(TaskInfo::add).orElse(new TaskInfo());
    }

    private void sendResponse(CrackHashWorkerResponse response) {
        log.info("send response {}", response);
        resultSenderService.publish(response);
    }
}
