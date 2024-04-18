package school.hei.haapi.endpoint.rest.http;

import java.net.http.HttpClient;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class HttpRequester {
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String SANDBOX_URL = "https://devapi.mvola.mg";
  private final String PRODUCTION_URL = "https://api.mvola.mg";
}
