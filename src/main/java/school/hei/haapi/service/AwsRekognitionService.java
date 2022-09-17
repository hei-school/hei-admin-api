package school.hei.haapi.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.exception.BadRequestException;

import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.util.List;

@Service
public class AwsRekognitionService {
    @Autowired
    private AmazonRekognition rekognitionClient;
    @Autowired
    private AwsS3Service awsS3Service;

    public S3ObjectSummary compareFacesMatches(byte[] sourceImage) throws NoSuchFileException {
        ByteBuffer sourceImageBuffered = ByteBuffer.wrap(sourceImage);
        Image imageSource = new Image().withBytes(sourceImageBuffered);
        Float similarityThreshold = 80F;

        List<S3ObjectSummary> s3ObjectSummaries = awsS3Service.listObjects();

        for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
            ByteBuffer targetImageBuffered = ByteBuffer.wrap(awsS3Service.getImage(s3ObjectSummary.getKey()));
            Image imageTarget = new Image().withBytes(targetImageBuffered);

            CompareFacesRequest request = new CompareFacesRequest()
                    .withSourceImage(imageSource)
                    .withTargetImage(imageTarget)
                    .withSimilarityThreshold(similarityThreshold);

            try {
                CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);
                List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
                List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();
                if (faceDetails.get(0).getSimilarity() >= similarityThreshold) {
                    return s3ObjectSummary;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        throw new BadRequestException("NOT FOUND");
    }
}
