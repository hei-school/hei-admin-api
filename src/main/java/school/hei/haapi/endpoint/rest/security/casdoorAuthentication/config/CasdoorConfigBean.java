package school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config;

import org.casbin.casdoor.config.CasdoorConfig;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasdoorConfigBean {
  private final CertificateLoader certificateLoader;

  @Value("${CASDOOR_ENDPOINT}")
  private String endpoint;

  @Value("${CASDOOR_CLIENT_ID}")
  private String clientId;

  @Value("${CASDOOR_CLIENT_SECRET}")
  private String clientSecret;

  @Value("${CASDOOR_CERTIFICATE}")
  private String cert;

  @Value("${CASDOOR_ORGANIZATION_NAME}")
  private String orgName;

  @Value("${CASDOOR_APPLICATION_NAME}")
  private String appName;

  public CasdoorConfigBean(CertificateLoader certificateLoader) {
    this.certificateLoader = certificateLoader;
  }

  @Bean
  public CasdoorAuthService casdoorAuthService() {
    cert = certificateLoader.getCertificate();
    CasdoorConfig config =
        new CasdoorConfig(endpoint, clientId, clientSecret, cert, orgName, appName);
    return new CasdoorAuthService(config);
  }
}
