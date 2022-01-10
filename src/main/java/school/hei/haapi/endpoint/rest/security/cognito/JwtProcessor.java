package school.hei.haapi.endpoint.rest.security.cognito;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JwtProcessor {
  private final JWSAlgorithm rs256;
  private final Integer connectTimeout;
  private final Integer readTimeout;
  private final JwtConfiguration jwtConfiguration;

  public JwtProcessor(
      @Value("${aws.cognito.jwt.jwsAlgorithm}") final String algorithm,
      @Value("${aws.cognito.jwt.connectTimeout}") final Integer connectTimeout,
      @Value("${aws.cognito.jwt.readTimeout}") final Integer readTimeOut,
      final JwtConfiguration jwtConfiguration) {
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeOut;
    this.rs256 = new JWSAlgorithm(algorithm);
    this.jwtConfiguration = jwtConfiguration;
  }

  @Bean
  public ConfigurableJWTProcessor<SecurityContext> configurableJwtProcessor()
      throws MalformedURLException {
    ResourceRetriever resourceRetriever =
        new DefaultResourceRetriever(this.connectTimeout, this.readTimeout);
    URL jwkUrl = new URL(jwtConfiguration.getCognitoJwksUrlFormat());
    JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwkUrl, resourceRetriever);
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<>(this.rs256, keySource);
    jwtProcessor.setJWSKeySelector(keySelector);
    return jwtProcessor;
  }
}
