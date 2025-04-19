package ru.nsu.leontev.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.nsu.leontev.model.Status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class TaskInfo {
    @Id
    private String id;
    private String hash;
    private int maxLength;
    private String alphabet;
    private LocalDateTime startedAt;
    private Status status;
    private long[] wordsComplete;
    private Set<String> data;

    public TaskInfo(String id, String hash, int maxLength, int workerCount, String alphabet) {
        this.id = id;
        this.hash = hash;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
        wordsComplete = new long[workerCount];
        data = new HashSet<>();
    }

    public double getComplete() {
        return Arrays.stream(wordsComplete).sum() / (double) getAllCombsCount();
    }

    private long getAllCombsCount() {
        return (long) (Math.pow(alphabet.length(), maxLength + 1) - alphabet.length()) / (alphabet.length() - 1);
    }
}
