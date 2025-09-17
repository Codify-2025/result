package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class UnauthenticatedException extends BaseException {
    public UnauthenticatedException() {
        super(ErrorCode.UNAUTHENTICATED);
    }
}
