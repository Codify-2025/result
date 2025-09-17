package Codify.result.web.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveResultRequestDto {
    private boolean plagiarize;
    private StudentInfo student1;
    private StudentInfo student2;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String name;
        private String fileName;
        private String submittedTime;
    }
}
