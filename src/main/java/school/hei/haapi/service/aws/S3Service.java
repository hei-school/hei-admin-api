package school.hei.haapi.service.aws;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import school.hei.haapi.file.BucketComponent;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.file.S3Conf;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Service
@AllArgsConstructor
public class S3Service {
  private BucketComponent bucketComponent;
  private S3Conf s3Conf;

  public String getPresignedUrl(String key, Long durationExpirationSeconds) {
    Instant now = Instant.now();
    Instant expirationInstant = now.plusSeconds(durationExpirationSeconds);
    Duration expirationDuration = Duration.between(now, expirationInstant);
    return bucketComponent.presign(key, expirationDuration).toString();
  }

  public String uploadObjectToS3Bucket(String key, byte[] bytes) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3Conf.getS3BucketName())
            .key(key)
            .checksumAlgorithm(ChecksumAlgorithm.SHA256)
            .build();
    PutObjectResponse response =
        s3Conf.getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(bytes));

    ResponseOrException<HeadObjectResponse> responseOrException =
        s3Conf
            .getS3Client()
            .waiter()
            .waitUntilObjectExists(
                HeadObjectRequest.builder().bucket(s3Conf.getS3BucketName()).key(key).build())
            .matched();

    responseOrException
        .exception()
        .ifPresent(
            throwable -> {
              throw new ApiException(SERVER_EXCEPTION, throwable.getMessage());
            });

    return response.checksumSHA256();
  }
}
