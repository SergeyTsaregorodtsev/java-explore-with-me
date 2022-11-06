package ru.practicum.ewm.service.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.comment.CommentDto;
import ru.practicum.ewm.model.comment.CommentMapper;
import ru.practicum.ewm.repository.CommentRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCommentService {
    CommentRepository commentRepository;

    // Получение комментариев к событию с возможностью фильтрации
    public List<CommentDto> getComments(long eventId, String estimation, int from, int size) {
        Sort sortingBy = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortingBy);
        Page<Comment> comments;
        try {
            Comment.Estimation est = Comment.Estimation.valueOf(estimation);
            comments = commentRepository.findAllByEvent_IdAndEstimation(eventId, est, page);
        } catch (IllegalArgumentException ignore) {
            comments = commentRepository.findAllByEvent_Id(eventId, page);
        }
        log.trace("По запросу '{}' получено {} комментариев.", estimation, comments.getContent().size());
        return comments.getContent()
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    // Получение комментария
    public CommentDto getComment(long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new EntityNotFoundException("");
        });
        log.trace("По запросу получен комментарий ID {}", comment.getId());
        return CommentMapper.toDto(comment);
    }
}