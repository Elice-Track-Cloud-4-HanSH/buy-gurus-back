package com.team04.buy_gurus.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER("ROLE_USER"),
    SELLER("ROLE_SELLER"),
    ADMIN("ROLE_ADMIN");

    private final String value;
}