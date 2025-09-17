package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class WeekNotFoundException extends BaseException {
    public WeekNotFoundException() {
        super(ErrorCode.WEEK_NOT_FOUND);
    }
}
