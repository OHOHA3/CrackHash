package ru.nsu.leontev.rest.userinteraction.model;

import java.util.List;

public record CompleteResponse(long checkedCombs, List<String> matches) {
}
