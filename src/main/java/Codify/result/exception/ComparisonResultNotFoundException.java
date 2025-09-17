package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class ComparisonResultNotFoundException extends BaseException {
    public ComparisonResultNotFoundException() {
        super(ErrorCode.COMPARISON_RESULT_NOT_FOUND);
    }
}
