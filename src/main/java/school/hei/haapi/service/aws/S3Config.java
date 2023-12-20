package school.hei.haapi.service.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

  @Value("${aws.region}")
  private String awsS3Region;

  @Bean
  public S3Client getS3Client() {
    return S3Client.builder()
        .region(
            Region.of(
                awsS3Region)) // no need to add awsS3AccessKeyId and  awsS3SecretKey. AWS will find
                              // it automatically
        .build();
  }
}
