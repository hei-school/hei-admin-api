package school.hei.haapi.endpoint.rest.controller;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.AwsRekognitionService;

import java.nio.file.NoSuchFileException;

@RestController
public class AwsRekognitionController {
    @Autowired
    private AwsRekognitionService awsRekognitionService;

    @PostMapping(value = "/rekognition")
    public S3ObjectSummary getAll(@RequestBody byte[] image) throws NoSuchFileException {
        return awsRekognitionService.compareFacesMatches(image);
    }
}
