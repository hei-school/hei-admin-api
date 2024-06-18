package school.hei.haapi.service.mobileMoney.orange;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.mapper.ExternalExceptionMapper;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.OrangeTransactionDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;

@Component("OrangeApi")
@AllArgsConstructor
class OrangeApi implements MobileMoneyApi {
  private final ObjectMapper objectMapper;
  private final ExternalExceptionMapper exceptionMapper;
  private final ExternalResponseMapper responseMapper;

  private static final String baseUrl = "https://api.sandbox.orange-sonatel.com";
  private static final String OAUTH_PATH = "/oauth/token";
  public static final String API_WALLET_TRANSACTION_PATH =
      "/api/eWallet/v1/transactions?transactionId=%s";

  private String signinThenGenerateToken() {
    try (HttpClient httpClient = HttpClient.newHttpClient(); ) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + OAUTH_PATH))
              .POST(HttpRequest.BodyPublishers.noBody())
              .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      exceptionMapper.accept(response);
      return response.body();
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  @Override
  public TransactionDetails getByTransactionRef(MobileMoneyType type, String transactionRef)
      throws ApiException {
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

      exceptionMapper.accept(response);
      return responseMapper.from(
          objectMapper.readValue(response.body(), OrangeTransactionDetails.class));
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
