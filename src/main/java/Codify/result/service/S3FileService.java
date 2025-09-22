package Codify.result.service;

import Codify.result.exception.S3FileNotFoundException;
import Codify.result.exception.S3FileReadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService {

    private final S3Client s3Client;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public List<String> getCodeFromS3(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName) // S3 버킷명
                    .key(s3Key)         // 파일 경로/이름
                    .build();

            List<String> codeLines = new ArrayList<>();
            
            // 파일 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            s3Client.getObject(getObjectRequest), // S3에서 파일을 InputStream으로 가져옴
                            StandardCharsets.UTF_8
                    ))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    codeLines.add(line);
                }
            }
            
            return codeLines;
            
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", s3Key);
            throw new S3FileNotFoundException();
        } catch (IOException e) {
            log.error("Failed to read file from S3: {}", s3Key, e);
            throw new S3FileReadException();
        }
    }
    
    // 특정 라인 번호들에 해당하는 코드만 가져오기
    public List<String> getSpecificLinesFromS3(String s3Key, List<Integer> lineNumbers) {
        List<String> allLines = getCodeFromS3(s3Key);
        
        return lineNumbers.stream()
                .filter(lineNum -> lineNum > 0 && lineNum <= allLines.size())
                .map(lineNum -> allLines.get(lineNum - 1)) // 1-based -> 0-based 인덱스 변환
                .toList();
    }
}
