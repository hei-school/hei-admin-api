package school.hei.haapi.service.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import school.hei.haapi.file.BucketComponent;
import school.hei.haapi.file.S3Conf;
import school.hei.haapi.model.exception.ApiException;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Service
@AllArgsConstructor
public class FileService {
  private BucketComponent bucketComponent;
  private S3Conf s3Conf;

  public String getPresignedUrl(String key, Long durationExpirationSeconds) {
    Instant now = Instant.now();
    Instant expirationInstant = now.plusSeconds(durationExpirationSeconds);
    Duration expirationDuration = Duration.between(now, expirationInstant);
    return bucketComponent.presign(key, expirationDuration).toString();
  }

  public String uploadObjectToS3Bucket(String key, byte[] file) {
    return bucketComponent.upload(createTempFile(file), key).value().toString();
  }

  private File createTempFile(byte[] bytes) {
    File tempFile;
    try {
      tempFile = File.createTempFile("file", "temp");
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      outputStream.write(bytes);
      return  tempFile;
    }
    catch (IOException ioException) {
      throw new ApiException(SERVER_EXCEPTION, ioException.getMessage());
    }
  }
}
