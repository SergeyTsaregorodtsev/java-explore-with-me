package ru.practicum.ewm.statclient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewStats {
    String app;
    String uri;
    int hits;
}
