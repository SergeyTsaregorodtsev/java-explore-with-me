package ru.practicum.ewm.model.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.user.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto dto, User author, Event event) {
        return new Comment(
                author,
                event,
                LocalDateTime.now(),
                dto.getText(),
                Comment.Estimation.valueOf(dto.getEstimation().toUpperCase())
        );
    }

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                UserMapper.toShortDto(comment.getAuthor()),
                comment.getCreated().format(formatter),
                comment.getText(),
                comment.getEstimation().name());
    }
}
