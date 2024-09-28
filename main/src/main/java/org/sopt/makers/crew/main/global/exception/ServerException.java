package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

public class ServerException extends BaseException {
    public ServerException(HttpStatus httpStatus) {
        super(httpStatus);
    }

    public ServerException(String responseMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, responseMessage);
    }

}
