package ru.practicum.ewm.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventUserFilter {
    String text;
    long[] categories;
    Boolean paid;
    String rangeStart;
    String rangeEnd;
    boolean onlyAvailable;
}