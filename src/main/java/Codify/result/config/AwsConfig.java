package Codify.result.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.region:ap-northeast-2}")
    private String awsRegion;

    @Value("${aws.accessKey:}")
    private String awsAccessKey;

    @Value("${aws.secretKey:}")
    private String awsSecretKey;

    @Bean
    public S3Client s3Client() {
        if (awsAccessKey.isEmpty() || awsSecretKey.isEmpty()) {
            // 기본 credential provider chain 사용
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .build();
        } else {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
            
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
