package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.awt.image.ImagingOpException;
import java.util.Base64;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;

@Component
public class Base64Converter implements Function<Resource, String> {

  @Override
  @SneakyThrows
  public String apply(Resource resource) {
    try {
      byte[] file = resource.getInputStream().readAllBytes();
      return Base64.getEncoder().encodeToString(file);
    } catch (ImagingOpException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }
}
