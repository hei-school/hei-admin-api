package school.hei.haapi.service.utils;

import java.util.function.BiFunction;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ClassPathResourceResolver implements BiFunction<String, String, ClassPathResource> {
  @Override
  public ClassPathResource apply(String fileName, String fileType) {
    Resource resource = new ClassPathResource("static/images/" + fileName + fileType);
    return (ClassPathResource) resource;
  }
}
