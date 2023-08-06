package school.hei.haapi.service.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {


    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    private final S3Client s3Client;

    public String uploadObjectToS3Bucket(String key, byte[] file){
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(file));
            return key;
        } catch (AwsServiceException | SdkClientException e) {
            throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION,e);
        }
    }

    public byte[] getObjectFromS3Bucket(String key){
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build();
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
            return objectBytes.asByteArray();
        } catch (AwsServiceException | SdkClientException e) {
            throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION,e);
        }
    }
}
