package ru.nsu.leontev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.nsu.leontev.model.CrackRequest;
import ru.nsu.leontev.model.CrackStatusResponse;
import ru.nsu.leontev.service.CrackManagerService;
import ru.nsu.leontev.service.TaskInfoService;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/hash")
public class ExternalApiController {
    private final CrackManagerService crackManagerService;
    private final TaskInfoService taskInfoService;

    @GetMapping()
    public String menu(Model model,
                       @ModelAttribute("lastTaskId") String lastTaskId,
                       @ModelAttribute("crackRequest") CrackRequest crackRequest) {

        log.info("GET /api/hash called");

        model.addAttribute("taskIds", taskInfoService.getTaskIds());
        return "menu";
    }

    @PostMapping("/crack")
    public String crack(@ModelAttribute("crackRequest") CrackRequest crackRequest,
                        RedirectAttributes redirectAttributes) {

        log.info("POST /api/hash/crack called. Hash: {}, Max length: {}",
                crackRequest.getHash(), crackRequest.getMaxLength());

        String taskId = crackManagerService.handleTask(crackRequest.getHash(), crackRequest.getMaxLength());

        log.info("Created task with ID: {}", taskId);
        redirectAttributes.addFlashAttribute("lastTaskId", taskId);
        return "redirect:/api/hash";
    }

    @GetMapping("/status")
    public String status(@RequestParam String requestId, Model model) {

        log.info("GET /api/hash/status called with requestId: {}", requestId);

        CrackStatusResponse response = crackManagerService.getTaskStatus(requestId);

        model.addAttribute("response", response);
        return "status";
    }
}
