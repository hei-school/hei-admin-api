package school.hei.haapi.service.orange;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.MpReturnedType;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class OrangeApi {

  private static final String baseUrl = "https://api.sandbox.orange-sonatel.com";
  private static final String OAUTH_PATH = "/oauth/token";
  public static final String API_WALLET_TRANSACTION_PATH =
      "/api/eWallet/v1/transactions?transactionId=%s";
  private final ObjectMapper objectMapper;

  //TODO:
  private String signinThenGenerateToken() {
    try (HttpClient httpClient = HttpClient.newHttpClient(); ) {
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + OAUTH_PATH))
              .POST(HttpRequest.BodyPublishers.noBody())
              .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public MpReturnedType checkTransactionByRef(String transactionRef) {
    String token = signinThenGenerateToken();
    String path = API_WALLET_TRANSACTION_PATH.formatted(transactionRef);
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .setHeader("Authorization", token)
              .uri(URI.create(baseUrl + path))
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 400) {
        throw new BadRequestException(response.body());
      }
      return objectMapper.readValue(response.body(), MpReturnedType.class);
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
