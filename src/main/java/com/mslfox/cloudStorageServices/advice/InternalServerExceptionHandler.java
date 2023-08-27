package com.mslfox.cloudStorageServices.advice;

import com.mslfox.cloudStorageServices.exception.InternalServerException;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.model.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class InternalServerExceptionHandler {
    private final ErrorMessage errorMessage;

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(InternalServerException e) {
        return new ErrorResponse(e.getMessage(), 1L);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handle(Exception e) {
//        log.error(e.getMessage());
//        return new ErrorResponse(errorMessage.internalError(), 1L);
        return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse(errorMessage.internalError(), 1L));
    }

}
