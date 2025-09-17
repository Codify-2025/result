package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class StudentSubmissionNotFoundException extends BaseException {
    public StudentSubmissionNotFoundException() {
        super(ErrorCode.STUDENT_SUBMISSION_NOT_FOUND);
    }
}
