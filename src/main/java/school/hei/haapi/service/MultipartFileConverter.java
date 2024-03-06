package school.hei.haapi.service;

import static java.io.File.createTempFile;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.exception.ApiException;

@Component
public class MultipartFileConverter implements Function<MultipartFile, File> {
  @Override
  public File apply(MultipartFile multipartFile) {
    try {
      File tempFile =
          createTempFile(Objects.requireNonNull(multipartFile.getOriginalFilename()), null);
      multipartFile.transferTo(tempFile);
      return tempFile;
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
