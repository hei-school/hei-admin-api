package school.hei.haapi.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class CompareFaceService {

    private final school.hei.haapi.service.S3Service service;
    private String accessKeyId;

    private String secretKey;
    public static Float SIMILARITY_THRESHOLD = 80F;

    public CompareFaceService(school.hei.haapi.service.S3Service service,
                              @Value("${aws.access.key.id}")
                              String accessKeyId,
                              @Value("${aws.secret.key}")
                              String secretKey) {
        this.service = service;
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
    }

    public CompareFaceService(school.hei.haapi.service.
                                      S3Service service) {
        this.service = service;
    }

    public CompareFacesResult compareFaces(byte[] toCompare, String picture) {


        Regions region = Regions.EU_WEST_3;

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

        AmazonRekognition amazonRekognitionClient = AmazonRekognitionClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();


        byte[] reference = service.getImageFromS3(picture);

        ByteBuffer ref = ByteBuffer.wrap(reference);
        ByteBuffer toC = ByteBuffer.wrap(toCompare);

        CompareFacesRequest facesRequest = new CompareFacesRequest()
                .withSourceImage(new Image().withBytes(ref))
                .withTargetImage(new Image().withBytes(toC))
                .withSimilarityThreshold(SIMILARITY_THRESHOLD);

        CompareFacesResult result = amazonRekognitionClient.compareFaces(facesRequest);

        return result;
    }
}
