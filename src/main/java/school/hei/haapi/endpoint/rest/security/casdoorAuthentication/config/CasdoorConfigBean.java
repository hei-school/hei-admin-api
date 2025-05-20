package school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config;

import org.casbin.casdoor.config.CasdoorConfig;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasdoorConfigBean {
  private final CertificateLoader certificateLoader;
  private final String endpoint;
  private final String clientId;
  private final String clientSecret;
  private final String orgName;
  private final String appName;

  public CasdoorConfigBean(
      CertificateLoader certificateLoader,
      @Value("${CASDOOR_ENDPOINT}") String endpoint,
      @Value("${CASDOOR_CLIENT_ID}") String clientId,
      @Value("${CASDOOR_CLIENT_SECRET}") String clientSecret,
      @Value("${CASDOOR_ORGANIZATION_NAME}") String orgName,
      @Value("${CASDOOR_APPLICATION_NAME}") String appName) {
    this.certificateLoader = certificateLoader;
    this.endpoint = endpoint;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.orgName = orgName;
    this.appName = appName;
  }

  @Bean
  public CasdoorAuthService casdoorAuthService() {
    CasdoorConfig config =
        new CasdoorConfig(
            endpoint, clientId, clientSecret, certificateLoader.getCertificate(), orgName, appName);
    return new CasdoorAuthService(config);
  }
}
