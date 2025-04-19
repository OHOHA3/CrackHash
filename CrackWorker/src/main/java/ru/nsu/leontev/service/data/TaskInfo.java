package ru.nsu.leontev.service.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class TaskInfo {
    private long checkedCombs = 0;
    private final Set<String> matches = new HashSet<>();

    public void combChecked() {
        checkedCombs++;
    }

    public void addMatch(String match) {
        matches.add(match);
    }

    public TaskInfo add(TaskInfo other) {
        TaskInfo newTaskInfo = new TaskInfo();
        newTaskInfo.checkedCombs = checkedCombs + other.checkedCombs;
        newTaskInfo.matches.addAll(matches);
        newTaskInfo.matches.addAll(other.matches);
        return newTaskInfo;
    }
}
