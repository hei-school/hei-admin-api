package school.hei.haapi.endpoint;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToOcsDateFormat;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class OwnCloudConf {
  private final UriComponents baseUrl;
  @Getter private final String username;
  @Getter private final String password;

  public OwnCloudConf(
      @Value("${OWNCLOUD_BASE_URL}") String baseUrl,
      @Value("${OWNCLOUD_USERNAME}") String username,
      @Value("${OWNCLOUD_PASSWORD}") String password) {
    this.baseUrl =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/ocs/v1.php/apps/files_sharing/api/v1/shares")
            .queryParam("path={path}")
            .queryParam("name={name}")
            .queryParam("shareType=3")
            .queryParam("permissions={permissions}")
            .queryParam("expireDate={expireDate}")
            .queryParam("format=json")
            .queryParam("publicUpload=false")
            .build();
    this.username = username;
    this.password = password;
  }

  public URI getURI(String path, String name, Integer permissions) {
    Map<String, Object> uriVariables =
        Map.of(
            "path", path,
            "name", name,
            "permissions", permissions,
            "expireDate", instantToOcsDateFormat(Instant.now().plus(1, ChronoUnit.DAYS)));
    return baseUrl.expand(uriVariables).toUri();
  }
}
