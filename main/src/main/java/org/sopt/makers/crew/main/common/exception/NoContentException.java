package org.sopt.makers.crew.main.common.exception;

import org.springframework.http.HttpStatus;

public class NoContentException extends BaseException {

    public NoContentException() {
        super(HttpStatus.NO_CONTENT);
    }

    public NoContentException(String responseMessage) {
        super(HttpStatus.NO_CONTENT, responseMessage);
    }
}
