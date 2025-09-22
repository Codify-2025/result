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

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String awsRegion;

    @Value("${cloud.aws.credentials.access-key:}")
    private String awsAccessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
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
