package Codify.result.exception;

import Codify.result.exception.baseException.BaseException;

public class S3FileNotFoundException extends BaseException {
    public S3FileNotFoundException() {
        super(ErrorCode.S3_FILE_NOT_FOUND);
    }
}
