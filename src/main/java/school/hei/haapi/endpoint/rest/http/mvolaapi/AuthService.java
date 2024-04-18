package school.hei.haapi.endpoint.rest.http.mvolaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.http.HttpRequester;
import school.hei.haapi.endpoint.rest.http.type.AuthResponse;

@AllArgsConstructor
@Component
public class AuthService {
  private final HttpRequester httpRequester;
  @Autowired private final ObjectMapper objectMapper;

  public AuthResponse generateToken(String consumerKey, String consumerSecret)
      throws IOException, InterruptedException {
    String concatenatedKey = consumerKey + ":" + consumerSecret;
    String base64Encoded = Base64.getEncoder().encodeToString(concatenatedKey.getBytes());
    String basicAuthorizationToken = "Basic " + base64Encoded;
    String stringBody = new GenerateTokenParams().toString();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(httpRequester.getSANDBOX_URL() + "/token"))
            .header("Authorization", basicAuthorizationToken)
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(stringBody))
            .build();

    HttpResponse<String> response =
        httpRequester.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), AuthResponse.class);
  }

  @ToString
  @NoArgsConstructor
  static class GenerateTokenParams {
    public String grant_type = "client_credentials";
    public String scope = "EXT_INT_MVOLA_SCOPE";
  }
}
