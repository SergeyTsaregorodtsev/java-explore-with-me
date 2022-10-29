package ru.practicum.ewm.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiError {
    String status; // Код статуса HTTP-ответа (example: FORBIDDEN)
    String reason; // Общее описание причины ошибки
    String message; // Сообщение об ошибке
    String timestamp; // Дата и время когда произошла ошибка (в формате "yyyy-MM-dd HH:mm:ss")
}
