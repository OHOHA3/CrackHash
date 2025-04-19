package ru.nsu.leontev.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.nsu.leontev.AppContext;
import ru.nsu.leontev.db.entity.TaskInfo;
import ru.nsu.leontev.model.Status;
import ru.nsu.leontev.service.TaskInfoService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class TimeoutChecker {
    private final AppContext appContext;
    private final TaskInfoService taskInfoService;

    @Scheduled(fixedDelayString = "${app.checking-timeout-rate}")
    public void checkTaskTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        for (TaskInfo task : taskInfoService.findAllByStatus(Status.IN_PROGRESS)) {
            if (ChronoUnit.SECONDS.between(task.getStartedAt(), now) > appContext.getTimeForStopTask()) {
                if (task.getComplete() == 0) {
                    task.setStatus(Status.ERROR);
                } else {
                    task.setStatus(Status.PARTIALLY_READY);
                }
                taskInfoService.save(task);
            }
        }
    }
}
