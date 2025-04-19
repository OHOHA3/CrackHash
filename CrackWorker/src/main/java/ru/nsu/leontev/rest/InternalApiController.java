package ru.nsu.leontev.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.leontev.service.CrackWorkerService;
import ru.nsu.leontev.service.data.TaskInfo;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/internal/api/worker/hash/crack")
public class InternalApiController {
    private final CrackWorkerService crackWorkerService;

    @GetMapping("/task/{taskId}")
    public ResponseEntity<TaskInfo> info(@PathVariable String taskId) {
        log.info("Received status request for taskId={}. Val {}", taskId,
                crackWorkerService.getTaskInfo(taskId).getCheckedCombs());
        return ResponseEntity.ok(crackWorkerService.getTaskInfo(taskId));
    }
}
