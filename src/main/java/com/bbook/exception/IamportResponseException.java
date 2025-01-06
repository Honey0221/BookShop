package com.bbook.exception;

public class IamportResponseException extends Exception {
    private int code;

    public IamportResponseException(String message) {
        super(message);
        this.code = -1; // 기본 에러 코드
    }

    public int getCode() {
        return code;
    }
}