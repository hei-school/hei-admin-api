package school.hei.haapi.service.AWS;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import java.nio.ByteBuffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RekognitionService {

  public static Float SIMILARITY_THRESHOLD = 80F;
  private final S3Service service;
  private String accessKeyId;
  private String secretKey;
  private String region;

  public RekognitionService(S3Service service,
                            @Value("${aws.access.key.id}")
                            String accessKeyId,
                            @Value("${aws.secret.key}")
                            String secretKey,
                            @Value("${aws.region}")
                            String region
  ) {
    this.service = service;
    this.accessKeyId = accessKeyId;
    this.secretKey = secretKey;
    this.region = region;
  }

  public CompareFacesResult compareFaces(byte[] toCompare, String picture) {

    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

    AmazonRekognition amazonRekognitionClient = AmazonRekognitionClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();


    byte[] reference = service.getImage(picture);

    ByteBuffer referenceAsByteBuffer = ByteBuffer.wrap(reference);
    ByteBuffer targetImage = ByteBuffer.wrap(toCompare);

    CompareFacesRequest facesRequest = new CompareFacesRequest()
        .withSourceImage(new Image().withBytes(referenceAsByteBuffer))
        .withTargetImage(new Image().withBytes(targetImage))
        .withSimilarityThreshold(SIMILARITY_THRESHOLD);

    return amazonRekognitionClient.compareFaces(facesRequest);
  }
}
