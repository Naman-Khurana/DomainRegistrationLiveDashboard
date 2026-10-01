package com.project.DomainRegistrationLive.splitter;

import lombok.Getter;

@Getter
public class SplitterException extends RuntimeException {

    String errorCode;

    public SplitterException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SplitterException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}