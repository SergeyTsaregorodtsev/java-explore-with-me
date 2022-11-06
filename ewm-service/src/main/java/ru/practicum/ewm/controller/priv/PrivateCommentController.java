package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.comment.CommentDto;
import ru.practicum.ewm.model.comment.NewCommentDto;
import ru.practicum.ewm.model.comment.UpdateCommentRequest;
import ru.practicum.ewm.service.priv.PrivateCommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final PrivateCommentService service;

    //Добавление комментария к событию
    @PostMapping
    public CommentDto addComment(@PathVariable long userId,
                                 @Valid @RequestBody NewCommentDto dto) {
        log.trace("Получен POST-запрос на добавление комментария к событию ID {}", dto.getEventId());
        return service.addComment(userId, dto);
    }

    //Изменение комментария к событию
    @PatchMapping
    public CommentDto updateComment(@PathVariable long userId,
                                    @Valid @RequestBody UpdateCommentRequest dto) {
        log.trace("Получен PATCH-запрос на изменение комментария ID {}", dto.getCommentId());
        return service.updateComment(userId, dto);
    }


    //Получение всех своих комментариев
    @GetMapping
    public List<CommentDto> getComments(@PathVariable long userId,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос всех комментариев пользователя ID {}", userId);
        return service.getOwnComments(userId, from, size);
    }

    //Лайк чужого комментария - в разработке
    @PatchMapping("/{commentId}/like")
    public CommentDto addLike(@PathVariable long userId,
                              @PathVariable long commentId) {
        return service.addLike(userId, commentId);
    }

    //Отмена лайка чужого комментария - в разработке
    @PatchMapping("/{commentId}/dislike")
    public CommentDto removeLike(@PathVariable long userId,
                                 @PathVariable long commentId) {
        return service.removeLike(userId, commentId);
    }

    //Удаление своего комментария
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.trace("Получен DELETE-запрос на удаление собственного комментария ID {} от пользователя ID {}", commentId, userId);
        service.deleteComment(userId, commentId);
    }
}