package school.hei.haapi.service.telma;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import school.hei.haapi.http.model.TelmaAuthResponse;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.exception.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@AllArgsConstructor
public class MvolaApi {
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final String SANDBOX_URL = "https://devapi.mvola.mg";
    private final String PRODUCTION_URL = "https://api.mvola.mg";


    public TransactionDetails getTransactionById(String transactionId){
        try(HttpClient httpClient = HttpClient.newHttpClient()) {
            TelmaAuthResponse generatedToken = authService.generateToken(SANDBOX_URL);

            String endPath = "/mvola/mm/transactions/type/merchantpay/1.0.0/" + transactionId;

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(SANDBOX_URL + endPath))
                            .header("Authorization", generatedToken.getAccessToken())
                            //.header("Version", client.getHeadersOptions().getVersion())
                            //.header("X-CorrelationID", client.getHeadersOptions().getCorrelationId())
                            //.header("UserLanguage", client.getHeadersOptions().getUserLanguage())
                            //.header("UserAccountIdentifier", client.getHeadersOptions().getUserAccountIdentifier())
                            //.header("partnerName", client.getHeadersOptions().getPartnerName())
                            //.header("Cache-Control", client.getHeadersOptions().getCacheControl())
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), TransactionDetails.class);

        } catch (IOException | InterruptedException e) {
            throw new ApiException(SERVER_EXCEPTION, e);
        }
    }
}
