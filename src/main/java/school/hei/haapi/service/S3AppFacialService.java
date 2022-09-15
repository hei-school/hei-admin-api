package school.hei.haapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3AppFacialService {

    private final AmazonS3 amazonS3;


    @Value("${spring.aws.s3.bucket.name}")
    private String awsS3BucketName1;

    public List<S3ObjectSummary> getAll() {
        return amazonS3.listObjects(awsS3BucketName1).getObjectSummaries();
    }

    public byte[] getFileByNameAsByte(String name) throws IOException {
        return amazonS3.getObject(awsS3BucketName1, name).getObjectContent().readAllBytes();
    }

    public S3ObjectInputStream getFileByName(String name) {
        return amazonS3.getObject(awsS3BucketName1, name).getObjectContent();
    }

}
