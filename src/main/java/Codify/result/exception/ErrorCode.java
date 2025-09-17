package Codify.result.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E1", "올바르지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E2", "잘못된 HTTP 메서드를 호출했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E3", "서버 에러가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E4", "존재하지 않는 엔티티입니다."),

    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "A1", "존재하지 않는 아티클입니다."),

    // S3 관련 에러
    S3_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "S1", "S3에서 파일을 찾을 수 없습니다."),
    S3_FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S2", "S3 파일을 읽는 중 오류가 발생했습니다."),

    // 인증 관련 에러
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "U1", "인증되지 않은 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U2", "사용자를 찾을 수 없습니다."),

    // 비교 관련 에러
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ST1", "해당 학생을 찾을 수 없습니다."),
    COMPARISON_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "ST2", "해당 학생들의 비교 결과를 찾을 수 없습니다."),
    SAME_STUDENT_COMPARISON(HttpStatus.BAD_REQUEST, "ST3", "동일한 학생끼리는 비교할 수 없습니다."),
    STUDENT_SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ST4", "해당 과제에 대한 학생의 제출 결과를 찾을 수 없습니다."),

    // 주차 관련 에러
    INVALID_WEEK_PARAMETER(HttpStatus.BAD_REQUEST, "W1", "유효하지 않은 주차입니다."),
    WEEK_NOT_FOUND(HttpStatus.NOT_FOUND, "W2", "해당 과제에 대한 주차 정보를 찾을 수 없습니다.");

    private final HttpStatus status; //http 상태 코드
    private final String code; //에러 구분 코드
    private final String message; //사용자에게 보여줄 에러 메시지

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
