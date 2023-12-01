package org.sopt.makers.crew.main.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String errorCode;
    private T data;

    public static ApiResponse success(Object data) {
            return ApiResponse.builder()
            .data(data)
            .build();
            }


    public static ApiResponse fail(String errorCode) {
            return ApiResponse.builder()
            .errorCode(errorCode)
            .build();
            }
}
