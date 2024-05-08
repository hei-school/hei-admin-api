package school.hei.haapi.service.orange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.HttpClient;
import school.hei.haapi.http.HttpRequester;
import school.hei.haapi.http.model.MpReturnedType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@AllArgsConstructor
public class OrangeApi {
    private HttpRequester httpRequester;
    @Autowired
    private final ObjectMapper objectMapper;
    private HttpClient httpClient;

    public MpReturnedType checkTransactionByRef(String transactionRef) throws InterruptedException, IOException {
        httpClient.setBaseURL("https://api.sandbox.orange-sonatel.com/api/eWallet/v1");
        String paths = "/transactions?transactionId=" + transactionRef;

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader("Authorization", httpClient.getBearer())
                .uri(URI.create(httpClient.getBaseURL() + paths))
                .build();

        HttpResponse<String> response =
                httpRequester.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), MpReturnedType.class);
    }
}
