package org.sopt.makers.crew.main.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {

    HttpStatus httpStatus;
    String errorCode;

    public BaseException(HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
    }

    public BaseException(HttpStatus httpStatus, String errorCode) {
        super(errorCode);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
