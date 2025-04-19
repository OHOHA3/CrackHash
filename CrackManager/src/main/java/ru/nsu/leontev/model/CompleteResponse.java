package ru.nsu.leontev.model;

import java.util.Set;

public record CompleteResponse(long checkedCombs, Set<String> matches) {
}
