package com.example.authdemo.exception;

public class BusinessException extends BaseException {

    public BusinessException(int code, String message) {
        super(code, message);
    }
}
