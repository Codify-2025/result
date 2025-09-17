package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class StudentNotFoundException extends BaseException {
    public StudentNotFoundException() {
        super(ErrorCode.STUDENT_NOT_FOUND);
    }
}
