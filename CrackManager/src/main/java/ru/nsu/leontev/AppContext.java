package ru.nsu.leontev;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppContext {
    private List<String> workerUrls;
    private String workerUrn;
    private String alphabet;
    private int maxWorkingTasks;
    private int timeForStopTask;

    public int getNumWorkers() {
        return workerUrls.size();
    }

    public String getWorkerUrl(int workerId) {
        return workerUrls.get(workerId);
    }
}

