package school.hei.haapi.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.Present;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RekognitionAppFacialService {

    private final AmazonRekognition amazonRekognition;
    private final S3AppFacialService s3AppFacialService;

    @Value("${spring.aws.s3.bucket.name}")
    private String awsS3BucketName1;

    public List<Present> facialPresence(String idEvent, MultipartFile multipartFile, Float similarity) throws IOException {
        List<Present> result;
        Float similarityThreshold = similarity;

        result=compareTwoFaces(amazonRekognition, similarityThreshold, multipartFile);
        return result;
    }
    public List<Present> compareTwoFaces(AmazonRekognition rekClient, Float similarityThreshold, MultipartFile sourceImage) throws IOException {
        List<Present> t=new ArrayList<>();

        for (S3ObjectSummary i:s3AppFacialService.getAll()) {

            byte [] target = s3AppFacialService.getFileByNameAsByte(i.getKey());

            try {
                // Create an Image object for the source image.

                Image souImage=new Image()
                        .withBytes(ByteBuffer.wrap(sourceImage.getBytes()));
                Image tarImage=new Image().withBytes(ByteBuffer.wrap(target));


                CompareFacesRequest facesRequest = new CompareFacesRequest()
                        .withSourceImage(souImage)
                        .withTargetImage(tarImage)
                        .withSimilarityThreshold(similarityThreshold);
                // Compare the two images.
                CompareFacesResult compareFacesResult = rekClient.compareFaces(facesRequest);
                List <CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
                int number = 0;
                for (CompareFacesMatch match: faceDetails){
                    number+=1;
                    ComparedFace face= match.getFace();
                    Present present = new Present();
                    present.setS3name(i.getKey());
                    present.setFaceNumber(number);
                    present.setSimilarity(match.getSimilarity());
                    present.setBoundingBoxTop(face.getBoundingBox().getTop());
                    present.setBoundingBoxLeft(face.getBoundingBox().getLeft());
                    present.setBoundingBoxHeight(face.getBoundingBox().getHeight());
                    present.setBoundingBoxWidth(face.getBoundingBox().getWidth());
                    t.add(present);
                }
                List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

            } catch( FileNotFoundException e) {

            } catch (IOException ex) {
            }

        }
        return t;
    }

}
