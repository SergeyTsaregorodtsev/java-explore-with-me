package ru.practicum.ewm.controller.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.comment.CommentDto;
import ru.practicum.ewm.service.adm.AdminCommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final AdminCommentService service;

    //Получение всех комментариев пользователя
    @GetMapping("/{userId}")
    public List<CommentDto> getComments(@PathVariable long userId,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос на все комментарии пользователя ID {}", userId);
        return service.getComments(userId, from, size);
    }

    //Удаление комментария
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long commentId) {
        log.trace("Получен DELETE-запрос на удаление комментария ID {}", commentId);
        service.deleteComment(commentId);
    }
}