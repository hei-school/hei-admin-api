package school.hei.haapi.service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Service {
  private String bucketName;

  private String accessKeyId;

  private String secretKey;


  private String region;

  public S3Service(
      @Value("${s3.bucket.name}")
      String bucketName,
      @Value("${aws.access.key.id}")
      String accessKeyId,
      @Value("${aws.secret.key}")
      String secretKey,
      @Value("${aws.region}")
      String region) {
    this.bucketName = bucketName;
    this.accessKeyId = accessKeyId;
    this.secretKey = secretKey;
    this.region = region;
  }

  public byte[] getImage(String pictureName) {

    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();

    try {
      S3Object object = s3Client.getObject(bucketName, pictureName);
      S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
      return IOUtils.toByteArray(s3ObjectInputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
