package school.hei.haapi.service;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import com.amazonaws.AmazonServiceException;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
@Service
public class AwsService {
    private static final Logger LOG = LoggerFactory.getLogger(AwsService.class);

    @Autowired
    private AmazonS3 amazonS3;
    @Value("${s3.bucket.name}")
    private String s3BucketName;

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
      final File file = new File(multipartFile.getOriginalFilename());
      try (final FileOutputStream outputStream = new FileOutputStream(file)) {

        outputStream.write(multipartFile.getBytes());

      } catch (IOException e) {

        LOG.error("Error {} occurred while converting the multipart file", e.getLocalizedMessage());
      }
      return file;
    }
    @Async
    public S3ObjectInputStream findByName(String fileName) {
      LOG.info("Downloading file with name {}", fileName);
      return amazonS3.getObject(s3BucketName, fileName).getObjectContent();

    }

    @Async
    public void save(final MultipartFile multipartFile) {

      try {
        final File file = convertMultiPartFileToFile(multipartFile);
        final String fileName = LocalDateTime.now() + "_" + file.getName();
        LOG.info("Uploading file with name {}", fileName);
        final PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketName , fileName , file);
        amazonS3.putObject(putObjectRequest);
        Files.delete(file.toPath()); // Remove the file locally created in the project fold

      } catch (AmazonServiceException e) {

        LOG.error("Error {} occurred while uploading file", e.getLocalizedMessage());

      } catch (IOException ex) {

        LOG.error("Error {} occurred while deleting temporary file", ex.getLocalizedMessage());
      }

    }
}
