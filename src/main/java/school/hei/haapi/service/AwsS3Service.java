package school.hei.haapi.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

@Service
public class AwsS3Service {
    @Value("${s3.bucket.name}")
    private String bucketname;
    @Autowired
    private AmazonS3 amazonS3;

    public List<S3ObjectSummary> listObjects() {
        ObjectListing objectListing = this.amazonS3.listObjects(this.bucketname);
        return objectListing.getObjectSummaries();
    }

    public byte[] getImage(final String keyName) throws NoSuchFileException {
        try {
            byte[] content;
            final S3Object s3Object = amazonS3.getObject(bucketname, keyName);
            final S3ObjectInputStream stream = s3Object.getObjectContent();
            content = IOUtils.toByteArray(stream);
            s3Object.close();
            return content;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new NoSuchFileException("File Not Found");
            }
            throw new AmazonClientException(e);
        } catch (IOException | AmazonClientException ex) {
            throw new AmazonClientException(ex);
        }
    }
}
