package school.hei.haapi.model.psp.vola;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.hei.haapi.model.psp.vola.api.VolaClient;

@Configuration
public class VolaPspConf {

  private final String apiUrl;
  private final String apiKey;

  public VolaPspConf(
      @Value("${vola.api.url}") String apiUrl, @Value("${vola.api.key}") String apiKey) {
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
  }

  @Bean
  public VolaClient volaClient() {
    return new VolaClient(apiUrl, apiKey);
  }
}
