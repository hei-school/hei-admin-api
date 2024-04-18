package school.hei.haapi.endpoint.rest.http.mvolaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.http.HttpClient;
import school.hei.haapi.endpoint.rest.http.HttpRequester;
import school.hei.haapi.endpoint.rest.http.type.TransactionDetails;

@AllArgsConstructor
@Component
public class TransactionService {
  private final HttpRequester httpRequester;
  @Autowired private final ObjectMapper objectMapper;

  public TransactionDetails getTransactionById(String transactionId, HttpClient client)
      throws InterruptedException, IOException {
    String endPath = "/mvola/mm/transactions/type/merchantpay/1.0.0/" + transactionId;

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(httpRequester.getSANDBOX_URL() + endPath))
            .header("Authorization", client.getToken())
            .header("Version", client.getHeadersOptions().getVersion())
            .header("X-CorrelationID", client.getHeadersOptions().getCorrelationId())
            .header("UserLanguage", client.getHeadersOptions().getUserLanguage())
            .header("UserAccountIdentifier", client.getHeadersOptions().getUserAccountIdentifier())
            .header("partnerName", client.getHeadersOptions().getPartnerName())
            .header("Cache-Control", client.getHeadersOptions().getCacheControl())
            .build();

    HttpResponse<String> response =
        httpRequester.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), TransactionDetails.class);
  }
}
