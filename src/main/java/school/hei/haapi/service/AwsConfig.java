package school.hei.haapi.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {
    @Value("${spring.aws.accessId}")
    private String awsAccessKeyId;

    @Value("${spring.aws.secretId}")
    private String awsSecretKey;

    @Value("${spring.aws.s3.region}")
    private String awsS3Region;

    @Value("${spring.aws.rekognition.region}")
    private String awsRekognitionRegion;

    @Bean
    public AmazonS3 amazonS3Client() {
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKeyId,awsSecretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(awsS3Region)
                .build();
    }

    @Bean
    public AmazonRekognition rekognitionClient() {
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKeyId,awsSecretKey);
        return AmazonRekognitionClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(awsRekognitionRegion)
                .build();
    }

}
