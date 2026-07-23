package kinderland.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Cấu hình AWS/S3 của product-service.
 *
 * GIỮ NGUYÊN prefix "aws" đang dùng sẵn trong project (aws.access-key, aws.region,
 * aws.s3.bucket) — không đặt tên property mới để khỏi phải sửa config repo.
 *
 * @Validated + @NotBlank làm service FAIL-FAST ngay lúc khởi động nếu thiếu cấu hình,
 * thay vì chạy được rồi mới ném "Bucket cannot be empty" lúc người dùng upload ảnh.
 */
@Validated
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    @NotBlank(message = "must not be blank - set environment variable AWS_REGION")
    private String region;

    /** Để rỗng thì dùng default credentials chain của AWS SDK (IAM role, ~/.aws/credentials...). */
    private String accessKey;

    private String secretKey;

    @Valid
    private S3 s3 = new S3();

    public static class S3 {
        @NotBlank(message = "must not be blank - set environment variable AWS_S3_BUCKET (S3 bucket name)")
        private String bucket;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }
}
