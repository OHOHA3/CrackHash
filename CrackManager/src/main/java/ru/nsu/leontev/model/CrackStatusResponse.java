package ru.nsu.leontev.model;

public record CrackStatusResponse(String hash,
                                  String alphabet,
                                  Status status,
                                  double complete,
                                  String[] data) {
}
