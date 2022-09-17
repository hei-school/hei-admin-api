package school.hei.haapi.service.AWS;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ObjectResource;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class S3Service {
  @Value("${s3.bucket.name}")
  private String bucketName;

  @Value("${aws.access.key.id}")
  private String accessKeyId;

  @Value("${aws.secret.key}")
  private String secretKey;


  @Value("${aws.region}")
  private String region;
  public byte[] getImage (String pictureName){

    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();

    try{
      S3Object object = s3Client.getObject(bucketName, pictureName);
      S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
      return IOUtils.toByteArray(s3ObjectInputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
