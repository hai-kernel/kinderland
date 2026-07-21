package kinderland.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Khởi tạo S3Client (upload/xoá) + S3Presigner (sinh link ký tạm để xem ảnh).
 * Credentials & region lấy từ prefix aws.* trong application.yml (nạp từ biến môi trường AWS_*).
 */
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class S3Config {

    private final AwsProperties awsProperties;

    public S3Config(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(resolveCredentials()))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(resolveCredentials()))
                .build();
    }

    /**
     * Bucket/region đã được @Validated bảo đảm không rỗng lúc startup.
     * Credentials vẫn cho phép rỗng: môi trường thật có thể dùng IAM role thay vì static key.
     */
    private AwsBasicCredentials resolveCredentials() {
        String accessKey = awsProperties.getAccessKey();
        if (accessKey == null || accessKey.isBlank()) {
            return AwsBasicCredentials.create("dummy", "dummy");
        }
        return AwsBasicCredentials.create(accessKey, awsProperties.getSecretKey());
    }
}
