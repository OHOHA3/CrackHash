package ru.nsu.leontev.rest.workerinteraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.leontev.response.CrackHashWorkerResponse;
import ru.nsu.leontev.service.CrackManagerService;

import java.util.List;

@RestController
@RequestMapping("internal/api/manager/hash/crack")
public class InternalApiController {
    private final CrackManagerService crackManagerService;

    @Autowired
    public InternalApiController(CrackManagerService crackManagerService) {
        this.crackManagerService = crackManagerService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestBody CrackHashWorkerResponse response) {
        String requestId = response.getRequestId();
        int partNumber = response.getPartNumber();
        long combChecked = response.getCombChecked();
        List<String> words = response.getAnswers().getWords();

        crackManagerService.handleTaskResult(requestId, partNumber, combChecked, words);

        return ResponseEntity.ok().build();
    }
}
