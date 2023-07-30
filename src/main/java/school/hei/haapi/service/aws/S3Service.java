package school.hei.haapi.service.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.exception.BadRequestException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {


    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    private final S3Client s3Client;
    public List<String> getListBucketsName(){
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        return s3Client.listBuckets(listBucketsRequest).buckets().stream().map(Bucket::name).collect(Collectors.toList());
    }

    public void uploadObjectToS3Bucket(String key, MultipartFile file){
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));
        }catch (Exception e){
            throw new BadRequestException("incorrect file upload");
        }
    }
}
