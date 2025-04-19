package ru.nsu.leontev.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrackRequest {
    private String hash;
    private int maxLength;
}
