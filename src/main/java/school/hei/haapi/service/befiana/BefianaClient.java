package school.hei.haapi.service.befiana;

import static school.hei.haapi.service.utils.InstantUtils.UTC3;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class BefianaClient {
  private static final DateTimeFormatter SEND_AT_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(UTC3);

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String apiKey;

  public BefianaClient(RestTemplate restTemplate, String baseUrl, String apiKey) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  public BefianaSendResponse send(String phoneNumber, String message) {
    var body = Map.of("phone_number", phoneNumber, "message", message);
    return exchange("/api/smsko/v1/send/", HttpMethod.POST, body, BefianaSendResponse.class);
  }

  public BefianaSendBulkResponse sendBulk(List<String> phoneNumbers, String message) {
    var body =
        Map.of(
            "phone_numbers",
            phoneNumbers,
            "message",
            message,
            "send_at",
            SEND_AT_FORMAT.format(Instant.now()));
    return exchange(
        "/api/smsko/v1/sendbulk/", HttpMethod.POST, body, BefianaSendBulkResponse.class);
  }

  public int getBalance() {
    var response =
        exchange("/api/smsko/v1/balance/", HttpMethod.GET, null, BefianaBalanceResponse.class);
    if (response.getAvailableBalance() == null) {
      log.warn(
          "BEFIANA /balance/ response didn't match any known field alias — see"
              + " BefianaBalanceResponse. Treating balance as 0.");
      return 0;
    }
    return response.getAvailableBalance();
  }

  public BefianaDeliveryStatusResponse getDeliveryStatus(String callbackData) {
    var uri =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/smsko/v1/get-delivery-status/")
            .queryParam("callback_data", callbackData)
            .toUriString();
    try {
      var response =
          restTemplate.exchange(
              uri,
              HttpMethod.GET,
              new HttpEntity<>(headers()),
              BefianaDeliveryStatusResponse.class);
      return response.getBody();
    } catch (RestClientException e) {
      throw toDetailedBefianaException("BEFIANA get-delivery-status call failed", e);
    }
  }

  private <T> T exchange(String path, HttpMethod method, Object body, Class<T> responseType) {
    try {
      var response =
          restTemplate.exchange(
              baseUrl + path, method, new HttpEntity<>(body, headers()), responseType);
      return response.getBody();
    } catch (RestClientException e) {
      throw toDetailedBefianaException("BEFIANA call to " + path + " failed", e);
    }
  }

  private BefianaException toDetailedBefianaException(String context, RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      var statusCode = responseException.getStatusCode().value();
      var responseBody = responseException.getResponseBodyAsString();
      var detailedMessage = context + ": HTTP " + statusCode + " - " + responseBody;
      return new BefianaException(detailedMessage, statusCode, e);
    }
    return new BefianaException(context + ": " + e.getMessage(), null, e);
  }

  private HttpHeaders headers() {
    var headers = new HttpHeaders();
    headers.set("Authorization", apiKey);
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    return headers;
  }
}
