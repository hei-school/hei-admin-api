package school.hei.haapi.endpoint.rest.controller;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.AwsS3Service;

import java.nio.file.NoSuchFileException;
import java.util.List;

@RestController
public class AwsS3Controller {
    @Autowired
    private AwsS3Service awsS3Service;

    @GetMapping(value = "/bucket/summary")
    public List<S3ObjectSummary> getAllFile() {
        return awsS3Service.listObjects();
    }

    @GetMapping(value = "/bucket/objet/{name}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public byte[] getImage(@PathVariable String name) throws NoSuchFileException {
        return awsS3Service.getImage(name);
    }
}
