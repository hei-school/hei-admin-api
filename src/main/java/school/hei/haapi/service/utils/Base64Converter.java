package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.awt.image.ImagingOpException;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.bean.Base64EncoderBean;
import school.hei.haapi.model.exception.ApiException;

@Component
public class Base64Converter implements Function<ClassPathResource, String> {

  @Override
  @SneakyThrows
  public String apply(ClassPathResource path) {
    try {
      byte[] file = path.getInputStream().readAllBytes();
      return Base64EncoderBean.Base64Encoder().encodeToString(file);
    } catch (ImagingOpException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }
}
