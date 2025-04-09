package ru.nsu.leontev.service;

import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.response.CrackHashWorkerResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CrackWorkerService {
    private static final ConcurrentHashMap<UUID, TaskInfo> TASKS = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    @Value("${manager.url}")
    private String managerUrl;

    @Autowired
    public CrackWorkerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void crackHash(CrackHashManagerRequest request) {
        String taskId = request.getRequestId();
        List<String> alphabet = request.getAlphabet().getSymbols();
        int partNumber = request.getPartNumber();
        int partCount = request.getPartCount();
        int maxLength = request.getMaxLength();

        long totalCombs = countTotalCombs(maxLength, alphabet.size());
        long partSize = totalCombs / partCount;
        long startIndex = partNumber * partSize;
        long endIndex = (partNumber == partCount - 1) ? totalCombs : startIndex + partSize;

        TaskInfo taskInfo = new TaskInfo();
        TASKS.put(UUID.fromString(taskId), taskInfo);

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

    private long countTotalCombs(int maxLength, int alphabetSize) {
        long totalCombs = 0;
        for (int i = 0; i < maxLength; i++) {
            totalCombs = (totalCombs + 1) * alphabetSize;
        }
        return totalCombs;
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
        return TASKS.get(UUID.fromString(taskId));
    }

    private void sendResponse(CrackHashWorkerResponse response) {
        restTemplate.postForObject(managerUrl, response, Void.class);
    }
}
