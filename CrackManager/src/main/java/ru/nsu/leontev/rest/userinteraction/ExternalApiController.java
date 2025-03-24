package ru.nsu.leontev.rest.userinteraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.nsu.leontev.rest.userinteraction.model.CrackRequest;
import ru.nsu.leontev.rest.userinteraction.model.CrackStatusResponse;
import ru.nsu.leontev.service.CrackManagerService;
import ru.nsu.leontev.service.TaskInfo;

@Controller
@RequestMapping("/api/hash")
public class ExternalApiController {
    private final CrackManagerService crackManagerService;

    @Autowired
    public ExternalApiController(CrackManagerService crackManagerService) {
        this.crackManagerService = crackManagerService;
    }

    @GetMapping()
    public String menu(Model model,
                       @ModelAttribute("lastTaskId") String lastTaskId,
                       @ModelAttribute("crackRequest") CrackRequest crackRequest) {
        model.addAttribute("taskIds", crackManagerService.getTaskIds());
        return "menu";
    }

    @PostMapping("/crack")
    public String crack(@ModelAttribute("crackRequest") CrackRequest crackRequest,
                        RedirectAttributes redirectAttributes) {
        String taskId = crackManagerService.handleTask(crackRequest.getHash(), crackRequest.getMaxLength());
        redirectAttributes.addFlashAttribute("lastTaskId", taskId);
        return "redirect:/api/hash";
    }

    @GetMapping("/status")
    public String status(@RequestParam String requestId, Model model) {
        TaskInfo taskInfo = crackManagerService.getTaskStatus(requestId);
        CrackStatusResponse response = new CrackStatusResponse(
                taskInfo.getStatus(),
                taskInfo.getComplete(),
                taskInfo.getData().toArray(new String[0]));
        model.addAttribute("response", response);
        return "status";
    }
}