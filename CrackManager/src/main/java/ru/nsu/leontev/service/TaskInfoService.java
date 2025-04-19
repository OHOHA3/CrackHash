package ru.nsu.leontev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nsu.leontev.AppContext;
import ru.nsu.leontev.db.entity.TaskInfo;
import ru.nsu.leontev.db.repository.TaskInfoRepository;
import ru.nsu.leontev.exception.TaskNotFoundException;
import ru.nsu.leontev.model.Status;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskInfoService {
    private final TaskInfoRepository taskInfoRepository;
    private final AppContext appContext;

    public void save(TaskInfo taskInfo) {
        taskInfoRepository.save(taskInfo);
    }

    public List<String> getTaskIds() {
        return taskInfoRepository.findAll().stream().map(TaskInfo::getId).toList();
    }

    public TaskInfo findById(String id) {
        Optional<TaskInfo> taskInfo = taskInfoRepository.findById(id);
        if (taskInfo.isEmpty()) {
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
        return taskInfo.get();
    }

    public Optional<TaskInfo> findFirstByStatus(Status status) {
        return taskInfoRepository.findFirstByStatus(status);
    }

    public List<TaskInfo> findAllByStatus(Status status) {
        return taskInfoRepository.findAllByStatus(status);
    }

    public boolean canStartTask() {
        return taskInfoRepository.findAllByStatus(Status.IN_PROGRESS).size() < appContext.getMaxWorkingTasks();
    }
}
