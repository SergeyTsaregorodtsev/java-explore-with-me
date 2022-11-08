package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.comment.CommentDto;
import ru.practicum.ewm.service.pub.PublicCommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final PublicCommentService service;

    // Получение комментариев к событию с возможностью фильтрации
    @GetMapping("/{eventId}/all")
    public List<CommentDto> getComments(@PathVariable long eventId,
                                        @RequestParam(name = "estimation", defaultValue = "all") String estimation,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос всех комментариев события ID {}, est [{}], from [{}], size [{}].",
                eventId, estimation, from, size);
        return service.getComments(eventId, estimation.toUpperCase(), from, size);
    }

    // Получение комментария
    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable long commentId) {
        log.trace("Получен GET-запрос комментария ID {}", commentId);
        return service.getComment(commentId);
    }
}