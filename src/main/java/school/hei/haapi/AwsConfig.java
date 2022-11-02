package school.hei.haapi;

import org.springframework.context.annotation.Configuration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AwsConfig {

  @Configuration
  public class AwsConf {
    @Value("${access.key.id}")
    private String accessKeyId;

    @Value("${access.key.secret}")
    private String accessKeySecret;

    @Value("${s3.region.name}")
    private String s3RegionName;

    @Bean
    public AmazonS3 getAmazonS3Client() {
      final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId , accessKeySecret);
      // Get Amazon S3 client and return the S3 client object
      return AmazonS3ClientBuilder
              .standard()
              .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
              .withRegion(s3RegionName)
              .build();

    }
  }
}
