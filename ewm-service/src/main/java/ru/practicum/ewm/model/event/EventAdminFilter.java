package ru.practicum.ewm.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventAdminFilter {
    int[] users;
    String[] states;
    int[] categories;
    String rangeStart;
    String rangeEnd;
}