package ru.practicum.ewm.model.event;

import java.util.Comparator;

public class EventViewsComparator implements Comparator<EventShortDto> {
    @Override
    public int compare(EventShortDto o1, EventShortDto o2) {
        return Integer.compare(o1.getViews(), o2.getViews());
    }
}