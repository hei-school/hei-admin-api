package school.hei.haapi.service.telma;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.TelmaAuthResponse;
import school.hei.haapi.model.exception.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@AllArgsConstructor
@Component
public class AuthService {
    private final ObjectMapper objectMapper;
    private final String consumerKey = "consumerKey";
    private final String consumerSecret = "consumerSecret";

    //TODO: use env var for consumerKey and consumerSecret
    public TelmaAuthResponse generateToken(String baseUrl) {
        try(HttpClient httpClient = HttpClient.newHttpClient()) {
            String concatenatedKey = consumerKey + ":" + consumerSecret;
            String base64Encoded = Base64.getEncoder().encodeToString(concatenatedKey.getBytes());
            String basicAuthorizationToken = "Basic " + base64Encoded;
            String stringBody = new GenerateTokenParams().toString();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + "/token"))
                            .header("Authorization", basicAuthorizationToken)
                            .header("Content-type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(stringBody))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), TelmaAuthResponse.class);
        }
        catch (IOException | InterruptedException e) {
            throw new ApiException(SERVER_EXCEPTION, e);
        }
    }

    @ToString
    @NoArgsConstructor
    static class GenerateTokenParams {
        public String grant_type = "client_credentials";
        public String scope = "EXT_INT_MVOLA_SCOPE";
    }
}
