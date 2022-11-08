package ru.practicum.ewm.service.adm;

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
public class AdminCommentService {
    CommentRepository commentRepository;

    //Получение всех комментариев пользователя
    public List<CommentDto> getComments(long userId, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Page<Comment> comments = commentRepository.findAllByAuthor_Id(userId, page);
        log.trace("Получены {} комментариев пользователя ID {}", comments.getContent().size(), userId);
        return comments
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    //Удаление комментария
    public void deleteComment(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Error: указанный комментарий не существует.");
        }
        commentRepository.deleteById(commentId);
        log.trace("Удалён комментарий ID {}", commentId);
    }
}