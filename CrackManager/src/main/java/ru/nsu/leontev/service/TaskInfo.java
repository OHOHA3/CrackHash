package ru.nsu.leontev.service;

import lombok.AccessLevel;
import lombok.Getter;
import ru.nsu.leontev.rest.userinteraction.model.Status;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class TaskInfo {

    private final String id;
    private final String hash;
    private final int maxLength;
    private final String alphabet;
    private LocalDateTime createdAt;
    @Getter(AccessLevel.NONE)
    private final Status[] status;
    private final long[] wordsComplete;
    private final Set<String> data;

    public TaskInfo(String id, String hash, int maxLength, int workerCount, String alphabet) {
        this.id = id;
        this.hash = hash;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
        status = new Status[workerCount];
        Arrays.fill(status, Status.IN_PROGRESS);
        wordsComplete = new long[workerCount];
        data = new HashSet<>();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(Status status, int workerId) {
        this.status[workerId] = status;
    }

    public Status getPartStatus(int partId) {
        return status[partId];
    }

    public Status getStatus() {
        long inProgressCount = Arrays.stream(status).filter(s -> s == Status.IN_PROGRESS).count();
        long readyCount = Arrays.stream(status).filter(s -> s == Status.READY).count();
        long errorCount = Arrays.stream(status).filter(s -> s == Status.ERROR).count();

        if (inProgressCount > 0) {
            return Status.IN_PROGRESS;
        } else if (readyCount == status.length) {
            return Status.READY;
        } else if (errorCount == status.length) {
            return Status.ERROR;
        } else {
            return Status.PARTIALLY_READY;
        }
    }

    public void setWordsComplete(long wordsComplete, int workerId) {
        this.wordsComplete[workerId] = wordsComplete;
    }
    public double getComplete() {
        return Arrays.stream(wordsComplete).sum() / (double) countTotalCombs(maxLength, alphabet.length());
    }
    private long countTotalCombs(int maxLength, int alphabetSize) {
        long totalCombs = 0;
        for (int i = 0; i < maxLength; i++) {
            totalCombs = (totalCombs + 1) * alphabetSize;
        }
        return totalCombs;
    }
}