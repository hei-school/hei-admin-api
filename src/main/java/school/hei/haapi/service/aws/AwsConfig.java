package school.hei.haapi.service.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
@Configuration
public class AwsConfig {
    @Value("${AWS_ACCESS_KEY_ID}")
    private String awsS3AccessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String awsS3SecretKey;

    @Value("${AWS_S3_REGION}")
    private String awsS3Region;



    @Bean
    public S3Client amazonS3Client() {
       return S3Client.builder()
                .region(Region.of(awsS3Region))
                .endpointOverride(URI.create("https://s3."+awsS3Region+".amazonaws.com"))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsS3AccessKeyId,awsS3SecretKey)))
                .build();
    }

}
