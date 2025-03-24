package ru.nsu.leontev.service;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TaskInfo {
    private long checkedCombs = 0;
    private final List<String> matches = new ArrayList<>();

    public void combChecked() {
        checkedCombs++;
    }

    public void addMatch(String match) {
        matches.add(match);
    }
}
