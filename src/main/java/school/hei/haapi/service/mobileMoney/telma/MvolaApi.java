package school.hei.haapi.service.mobileMoney.telma;

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
import school.hei.haapi.http.model.TelmaAuthResponse;
import school.hei.haapi.http.model.TelmaHttpHeadersOptions;
import school.hei.haapi.http.model.TelmaTransactionDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;

@AllArgsConstructor
@Component("MvolaApi")
class MvolaApi implements MobileMoneyApi {
  private final ObjectMapper objectMapper;
  private final AuthService authService;
  private final ExternalExceptionMapper exceptionMapper;
  private final ExternalResponseMapper mapper;

  private final String SANDBOX_URL = "https://devapi.mvola.mg";
  private final String PRODUCTION_URL = "https://api.mvola.mg";

  @Override
  public TransactionDetails getByTransactionRef(MobileMoneyType type, String transactionId) {
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      TelmaAuthResponse generatedToken = authService.generateToken(SANDBOX_URL);
      TelmaHttpHeadersOptions httpHeadersOptions = new TelmaHttpHeadersOptions();

      String endPath = "/mvola/mm/transactions/type/merchantpay/1.0.0/" + transactionId;

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(SANDBOX_URL + endPath))
              .header("Authorization", generatedToken.getAccessToken())
              .header("Version", httpHeadersOptions.getVersion())
              .header("X-CorrelationID", httpHeadersOptions.getCorrelationId())
              .header("UserLanguage", httpHeadersOptions.getUserLanguage())
              .header("UserAccountIdentifier", httpHeadersOptions.getUserAccountIdentifier())
              .header("partnerName", httpHeadersOptions.getPartnerName())
              .header("Cache-Control", httpHeadersOptions.getCacheControl())
              .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      exceptionMapper.accept(response);
      return mapper.from(objectMapper.readValue(response.body(), TelmaTransactionDetails.class));
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
