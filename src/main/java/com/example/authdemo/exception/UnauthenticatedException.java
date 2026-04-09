package com.example.authdemo.exception;

public class UnauthenticatedException extends BaseException {

    public UnauthenticatedException(String message) {
        super(401, message);
    }
}
