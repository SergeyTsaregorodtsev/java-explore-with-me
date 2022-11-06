package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.comment.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findByIdAndAuthor_IdAndEvent_Id(long commentId, long userId, long eventId);

    Page<Comment> findAllByAuthor_Id(long userId, Pageable page);

    Page<Comment> findAllByEvent_Id(long eventId, Pageable page);

    List<Comment> findAllByEvent_Id(long eventId);

    Page<Comment> findAllByEvent_IdAndEstimation(long eventId, Comment.Estimation estimation, Pageable page);
}