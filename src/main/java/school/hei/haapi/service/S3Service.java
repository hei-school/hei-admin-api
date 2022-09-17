package school.hei.haapi.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class S3Service {
  @Value("${s3.bucket.name}")
  private String bucketName;

  public byte[] getImageFromS3 (String picName,BasicAWSCredentials credentials){


    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.EU_WEST_3)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
    try{
      S3Object picture = s3Client.getObject(bucketName, picName);
      S3ObjectInputStream s3ObjectInputStream = picture.getObjectContent();
      return IOUtils.toByteArray(s3ObjectInputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
