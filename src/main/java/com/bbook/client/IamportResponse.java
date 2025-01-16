package com.bbook.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IamportResponse<T> {
    private int code;
    private String message;
    private T response;

    @Override
    public String toString() {
        return "IamportResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", response=" + response +
                '}';
    }
}