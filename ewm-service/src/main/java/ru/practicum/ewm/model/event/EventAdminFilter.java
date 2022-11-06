package ru.practicum.ewm.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventAdminFilter {
    long[] users;
    String[] states;
    long[] categories;
    String rangeStart;
    String rangeEnd;
}