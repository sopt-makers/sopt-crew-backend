package org.sopt.makers.crew.main.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SuccessResponse<T> {
    private final T data;

    public static <T> SuccessResponse<T> of(T data){
        return new SuccessResponse<>(data);
    }
}
