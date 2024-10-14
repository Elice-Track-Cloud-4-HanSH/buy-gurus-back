package com.team04.buy_gurus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse<T> {

    private String message;
    private T data;
}
