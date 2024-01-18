package school.hei.haapi.model.bean;

import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Base64EncoderBean {
  @Bean
  public static Base64.Encoder Base64Encoder() {
    return Base64.getEncoder();
  }
}
