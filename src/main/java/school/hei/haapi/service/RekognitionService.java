package school.hei.haapi.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
@AllArgsConstructor
public class RekognitionService {
  @Value("${s3.bucket.name}")
  private String bucketName;

  @Value("${aws.access.key.id}")
  private String accessKeyId;

  @Value("${aws.secret.key}")
  private String secretKey;

  public static Float SIMILARITY_THRESHOLD = 80F;

  private final S3Service s3Service;

  public CompareFacesResult compareFaces(byte[] toCompare, String ref){
    Float similarityThreshold = 80F;
    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId,secretKey);

    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.EU_WEST_2)
        .build();

    String reference = ref+".jpg";

    byte[] referenceImage = s3Service.getImageFromS3(reference, credentials);
    ByteBuffer template = ByteBuffer.wrap(referenceImage);
    ByteBuffer toCheck = ByteBuffer.wrap(toCompare);

    CompareFacesRequest facesRequest = new CompareFacesRequest()
        .withSourceImage(new Image().withBytes(template))
        .withTargetImage(new Image().withBytes(toCheck))
        .withSimilarityThreshold(similarityThreshold);

    CompareFacesResult result = rekognitionClient.compareFaces(facesRequest);

    return result;
  }
}
