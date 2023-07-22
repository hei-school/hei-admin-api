package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.S3Service;

import java.util.List;

@RestController
@AllArgsConstructor
public class TranscriptVersionController {
    private final S3Service s3Service;

    @GetMapping(value = "/list-buckets-name")
    public List<String> getListBucket(){
        return s3Service.getListBucketsName();
    }
}
