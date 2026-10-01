package com.project.DomainRegistrationLive.exception;

import com.project.DomainRegistrationLive.splitter.SplitterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.project.DomainRegistrationLive.splitter.SplitConstants.SPLITTER_SERVICE_EXCEPTION;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getErrorCode(),ex.getMessage()));
    }


    @ExceptionHandler(SplitterException.class)
    public ResponseEntity<ErrorResponse> handleSplitterException(SplitterException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(ex.getErrorCode(),ex.getMessage()));
    }
}
