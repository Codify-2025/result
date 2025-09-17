package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class InvalidWeekParameterException extends BaseException {
    public InvalidWeekParameterException() {
        super(ErrorCode.INVALID_WEEK_PARAMETER);
    }
}
