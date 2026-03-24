package school.hei.haapi.model.psp.vola;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.hei.haapi.model.psp.vola.api.VolaClient;

@Configuration
public class VolaPspConf {

  @Bean
  public VolaClient volaClient(
      @Value("${VOLA_API_URL}") String baseUrl, @Value("${VOLA_API_KEY}") String apiKey) {
    return new VolaClient(baseUrl, apiKey);
  }

  @Bean
  public VolaPsp volaPsp(VolaClient volaClient) {
    return new VolaPsp(volaClient);
  }
}
