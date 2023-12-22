package school.hei.haapi.service.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class ByteTypeValidator {

  public static void isValid(byte[] pdfData, String type, String subType) {
    try {
      DefaultDetector detector = new DefaultDetector();
      Metadata metadata = new Metadata();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfData);
      MediaType mediaType = detector.detect(inputStream, metadata);

      boolean isValid = type.equals(mediaType.getType()) && subType.equals(mediaType.getSubtype());
      if (!isValid) {
        throw new BadRequestException("file is not a valid " + subType);
      }
    } catch (IOException ignored) {
    }
  }
}
