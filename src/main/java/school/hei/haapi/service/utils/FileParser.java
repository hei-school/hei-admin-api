package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.CLIENT_EXCEPTION;

import java.io.IOException;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.exception.ApiException;

@Component
public class FileParser implements Function<MultipartFile, byte[]> {
  @Override
  public byte[] apply(MultipartFile multipartFile) {
    try {
      return multipartFile.getBytes();
    } catch (IOException e) {
      throw new ApiException(CLIENT_EXCEPTION, e);
    }
  }
}
