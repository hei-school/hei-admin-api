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
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
public class CognitoConf {

  private final String region;
  private final String userPoolId;

  private final JWSAlgorithm rs256;
  private final Integer connectTimeout;
  private final Integer readTimeout;

  public CognitoConf(
      @Value("${aws.region}") String region,
      @Value("${aws.cognito.userPool.id}") String userPoolId,
      @Value("${aws.cognito.jwt.jwsAlgorithm}") final String algorithm,
      @Value("${aws.cognito.jwt.connectTimeout}") final Integer connectTimeout,
      @Value("${aws.cognito.jwt.readTimeout}") final Integer readTimeOut) {
    this.region = region;
    this.userPoolId = userPoolId;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeOut;
    this.rs256 = new JWSAlgorithm(algorithm);
  }

  @Bean
  public ConfigurableJWTProcessor<SecurityContext> getJwtProcessor() {
    ResourceRetriever resourceRetriever =
        new DefaultResourceRetriever(this.connectTimeout, this.readTimeout);
    URL jwkUrl = getCognitoJwksUrlFormat();
    JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwkUrl, resourceRetriever);
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<>(this.rs256, keySource);
    jwtProcessor.setJWSKeySelector(keySelector);
    return jwtProcessor;
  }

  @Bean
  public CognitoIdentityProviderClient getCognitoClient() {
    return CognitoIdentityProviderClient.builder().region(Region.of(region)).build();
  }

  public String getUserPoolUrl() {
    return String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
  }

  private URL getCognitoJwksUrlFormat() {
    try {
      return new URL(getUserPoolUrl() + "/.well-known/jwks.json");
    } catch (MalformedURLException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public String getUserPoolId() {
    return userPoolId;
  }
}
