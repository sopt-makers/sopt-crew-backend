package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException{
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
