package ru.practicum.ewm.service.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.comment.*;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateCommentService {
    CommentRepository commentRepository;
    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    //Добавление комментария к событию
    public CommentDto addComment(long userId, NewCommentDto dto) {
        // Заявка пользователя на участие должна существовать и быть одобрена
        ParticipationRequest request = requestRepository.findByEvent_IdAndRequester_Id(dto.getEventId(), userId);
        if (request == null || request.getStatus() != ParticipationRequest.Status.CONFIRMED) {
            throw new ForbiddenRequestException("Error: User can't comment this event.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("User with requested ID not found.");
        });
        Event event = eventRepository.findById(dto.getEventId()).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with requested ID not found.");
        });
        Comment comment = commentRepository.save(CommentMapper.toComment(dto, user, event));
        log.trace("Добавлен комментарий ID {} от пользователя ID {}.", comment.getId(), comment.getAuthor().getId());
        return CommentMapper.toDto(comment);
    }

    //Изменение комментария к событию
    public CommentDto updateComment(long userId, UpdateCommentRequest dto) {
        Comment comment = commentRepository.findByIdAndAuthor_IdAndEvent_Id(
                dto.getCommentId(), userId, dto.getEventId());
        if (comment == null) {
            throw new RuntimeException("Comment update error.");
        }
        String text = dto.getText();
        if (text != null) {
            comment.setText(text);
        }
        String estimation = dto.getEstimation();
        if (estimation != null) {
            try {
                Comment.Estimation est = Comment.Estimation.valueOf(dto.getEstimation().toUpperCase());
                comment.setEstimation(est);
            } catch (IllegalArgumentException e) {
                throw new ForbiddenRequestException("Commentary field 'estimation' incorrect.");
            }
        }
        Comment updatedComment = commentRepository.save(comment);
        log.trace("Изменён комментарий ID {}.", updatedComment.getId());
        return CommentMapper.toDto(updatedComment);
    }

    //Получение всех своих комментариев
    public List<CommentDto> getOwnComments(long userId, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Page<Comment> commentPage = commentRepository.findAllByAuthor_Id(userId, page);
        log.trace("Получено {} комментариев пользователя ID {}.", commentPage.getContent().size(), userId);
        return commentPage
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    //Лайк чужого комментария - в разработке
    public CommentDto addLike(long userId, long commentId) {
        return null;
    }

    //Отмена лайка чужого комментария - в разработке
    public CommentDto removeLike(long userId, long commentId) {
        return null;
    }

    //Удаление своего комментария
    public void deleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new EntityNotFoundException("Comment with requested ID not found.");
        });
        if (comment.getAuthor().getId() != userId) {
            throw new ForbiddenRequestException("Error: can't delete other user's comment.");
        }
        commentRepository.deleteById(commentId);
        log.trace("Удалён комментарий ID {}.", commentId);
    }
}