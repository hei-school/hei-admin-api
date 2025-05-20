package school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CertificateLoader {

  @Value("${CASDOOR_CERTIFICATE}")
  private String certBase64;

  @Getter private String certificate;

  @PostConstruct
  public void init() {
    this.certificate = new String(java.util.Base64.getDecoder().decode(certBase64));
  }
}
