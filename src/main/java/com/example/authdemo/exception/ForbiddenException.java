package com.example.authdemo.exception;

public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(403, message);
    }
}
