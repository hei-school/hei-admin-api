package school.hei.haapi.service.befiana;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BefianaConf {
  private final String apiUrl;
  private final String apiKey;

  public BefianaConf(
      @Value("${befiana.api.url}") String apiUrl, @Value("${befiana.api.key}") String apiKey) {
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
  }

  @Bean
  public BefianaClient befianaClient() {
    return new BefianaClient(new RestTemplate(), apiUrl, apiKey);
  }
}
