package school.hei.haapi.service.aws;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.event.S3Conf;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@AllArgsConstructor
public class S3Service {
  private S3Conf s3Conf;

  public String getPresignedUrl(String key, Long durationExpirationSeconds) {
    Instant now = Instant.now();
    Instant expirationInstant = now.plusSeconds(durationExpirationSeconds);
    Duration expirationDuration = Duration.between(now, expirationInstant);

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(s3Conf.getS3BucketName()).key(key).build();
    PresignedGetObjectRequest presignedRequest = null;

    try {
      presignedRequest =
          s3Conf
              .getS3Presigner()
              .presignGetObject(
                  GetObjectPresignRequest.builder()
                      .signatureDuration(expirationDuration)
                      .getObjectRequest(getObjectRequest)
                      .build());
      return presignedRequest.url().toString();
    } catch (S3Exception e) {
      return null;
    }
  }

  public String uploadObjectToS3Bucket(String key, byte[] file) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3Conf.getS3BucketName())
            .key(key)
            .checksumAlgorithm(ChecksumAlgorithm.SHA256)
            .build();
    PutObjectResponse response =
        s3Conf.getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(file));

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
