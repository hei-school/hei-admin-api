package school.hei.haapi.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Present;
import school.hei.haapi.repository.EventParticipantRepository;

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

    private final StudentGroupService studentGroupService;
    private final EventService eventService;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventParticipantService eventParticipantService;

    @Value("${spring.aws.s3.bucket.name}")
    private String awsS3BucketName1;

    @Transactional
    public List<Present> facialPresence(String idEvent, MultipartFile multipartFile, Float similarity) throws IOException {
        //Event event = eventService.getById(idEvent);
        //List<String> allParticipantImageRef= new ArrayList<>();
        //List<S3ObjectSummary> allImages = s3AppFacialService.getAll();
        //List<User> participants = eventParticipantService.getParticipantsByEventId(idEvent);

        //if (eventParticipantService.getEventParticipantsByEventId(idEvent)==null){
        if (eventParticipantService.getEventParticipantsByEventId(idEvent)==null){
            throw new RuntimeException();
        }
        //List<EventParticipant> eventParticipants = eventParticipantService.getEventParticipantsByEventId(idEvent);
        List<EventParticipant> eventParticipants = eventParticipantRepository.getByEventId(idEvent);
        List<Present> result;
        Float similarityThreshold = similarity;
        result=compareTwoFaces(amazonRekognition, similarityThreshold, multipartFile,eventParticipants);

        return result;
    }
    public List<Present> compareTwoFaces(AmazonRekognition rekClient, Float similarityThreshold, MultipartFile sourceImage,List<EventParticipant> eventParticipants ) throws IOException {
        List<Present> t=new ArrayList<>();

        for (EventParticipant i:eventParticipants) {

            byte [] target = s3AppFacialService.getFileByNameAsByte(i.getParticipant().getRefImage());

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
                for (CompareFacesMatch match: faceDetails){
                    ComparedFace face= match.getFace();
                    Present present = new Present();
                    present.setS3name(i.getParticipant().getRefImage());
                    present.setStudentId(i.getParticipant().getId());
                    present.setSimilarity(match.getSimilarity());
                    present.setBoundingBoxTop(face.getBoundingBox().getTop());
                    present.setBoundingBoxLeft(face.getBoundingBox().getLeft());
                    present.setBoundingBoxHeight(face.getBoundingBox().getHeight());
                    present.setBoundingBoxWidth(face.getBoundingBox().getWidth());
                    t.add(present);
                }
                i.setStatus(school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum.valueOf("HERE"));

                List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

            } catch( FileNotFoundException e) {

            } catch (IOException ex) {
            }

        }
        return t;
    }

}
