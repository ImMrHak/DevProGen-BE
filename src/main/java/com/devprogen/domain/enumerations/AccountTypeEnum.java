package com.devprogen.domain.enumerations;

public enum AccountTypeEnum {
    GOOGLE, FACEBOOK, GITHUB, REGULAR;

    public static AccountTypeEnum fromString(String loginType) {
        for (AccountTypeEnum type : AccountTypeEnum.values()) {
            if (type.name().equalsIgnoreCase(loginType)) {
                return type;
            }
        }
        return REGULAR;
    }
}
