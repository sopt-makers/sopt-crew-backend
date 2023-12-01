package org.sopt.makers.crew.main.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final boolean success;
    private final String message;
    private T data;

    public static ApiResponse success(HttpStatus status, Object data) {
            return ApiResponse.builder()
            .status(status.value())
            .success(true)
            .data(data)
            .build();
            }


    public static ApiResponse fail(int status, String message) {
            return ApiResponse.builder()
            .status(status)
            .success(false)
            .message(message)
            .build();
            }
}
