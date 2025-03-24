package ru.nsu.leontev.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.leontev.request.CrackHashManagerRequest;
import ru.nsu.leontev.service.CrackWorkerService;
import ru.nsu.leontev.service.TaskInfo;

@RestController
@RequestMapping("/internal/api/worker/hash/crack")
public class InternalApiController {

    private final CrackWorkerService crackWorkerService;

    @Autowired
    public InternalApiController(CrackWorkerService crackWorkerService) {
        this.crackWorkerService = crackWorkerService;
    }

    @PostMapping("/task")
    public ResponseEntity<Void> task(@RequestBody CrackHashManagerRequest request) {
        crackWorkerService.crackHash(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<TaskInfo> info(@PathVariable String taskId) {
        return ResponseEntity.ok(crackWorkerService.getTaskInfo(taskId));
    }
}
