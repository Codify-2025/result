package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class SameStudentComparisonException extends BaseException {
    public SameStudentComparisonException() {
        super(ErrorCode.SAME_STUDENT_COMPARISON);
    }
}
