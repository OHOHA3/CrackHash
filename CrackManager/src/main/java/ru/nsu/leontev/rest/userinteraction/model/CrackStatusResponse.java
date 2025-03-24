package ru.nsu.leontev.rest.userinteraction.model;


public record CrackStatusResponse(Status status, double complete, String[] data) {
}
