package ru.practicum.ewm.common;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(final EntityNotFoundException e) {
        log.trace("Exception: " + HttpStatus.NOT_FOUND.name());
        return new ApiError(
                e.getStackTrace(),
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        log.trace("Exception: " + HttpStatus.CONFLICT.name());
        return new ApiError(
                e.getStackTrace(),
                HttpStatus.CONFLICT.name(),
                "Integrity constraint has been violated.",
                e.getCause().getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenRequestException(final ForbiddenRequestException e) {
        log.trace("Exception: " + HttpStatus.FORBIDDEN.name());
        return new ApiError(
                e.getStackTrace(),
                HttpStatus.FORBIDDEN.name(),
                "For the requested operation the conditions are not met.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.trace("Exception: " + HttpStatus.FORBIDDEN.name());
        return new ApiError(
                e.getStackTrace(),
                HttpStatus.BAD_REQUEST.name(),
                "Request parameters not valid.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleStatServerException(final StatServerException e) {
        log.trace("Exception: " + HttpStatus.INTERNAL_SERVER_ERROR.name());
        return new ApiError(
                e.getStackTrace(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Error occurred",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }
}