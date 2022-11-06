package ru.practicum.ewm.model.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class EventDateComparator implements Comparator<EventShortDto> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public int compare(EventShortDto o1, EventShortDto o2) {
        LocalDateTime date1 = LocalDateTime.parse(o1.getEventDate(), formatter);
        LocalDateTime date2 = LocalDateTime.parse(o2.getEventDate(), formatter);
        if (date1.isAfter(date2)) {
            return 1;
        }
        if (date1.isBefore(date2)) {
            return -1;
        }
        return 0;
    }
}