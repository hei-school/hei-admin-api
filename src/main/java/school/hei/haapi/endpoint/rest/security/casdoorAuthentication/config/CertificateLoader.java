package school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config;

import java.util.Base64;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CertificateLoader {

  @Getter private final String certificate;

  public CertificateLoader(@Value("${CASDOOR_CERTIFICATE}") String certBase64) {
    this.certificate = new String(Base64.getDecoder().decode(certBase64));
  }
}
