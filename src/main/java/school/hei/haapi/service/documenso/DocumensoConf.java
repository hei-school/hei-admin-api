package school.hei.haapi.service.documenso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumensoConf {

  private final String apiUrl;
  private final String apiKey;

  public DocumensoConf(
      @Value("${documenso.api.url}") String apiUrl, @Value("${documenso.api.key}") String apiKey) {
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
  }

  @Bean
  public DocumensoClient documensoClient() {
    return new DocumensoClient(apiUrl, apiKey);
  }
}
