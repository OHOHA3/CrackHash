package ru.nsu.leontev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EndpointSolver {
    private final List<String> urls;
    private final String taskUrn;

    @Autowired
    public EndpointSolver(@Value("${worker.urls}") String urls,
                          @Value("${worker.urn}") String taskUrn) {
        this.urls = List.of(urls.split(","));
        this.taskUrn = taskUrn;
    }

    public String getTaskEndpoint(int partNumber) {
        return urls.get(partNumber) + "/" + taskUrn;
    }

    public String getStatusEndpoint(int partNumber, String taskId) {
        return getTaskEndpoint(partNumber) + "/" + taskId;
    }
}
