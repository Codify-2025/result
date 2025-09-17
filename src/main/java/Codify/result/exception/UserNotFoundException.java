package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
