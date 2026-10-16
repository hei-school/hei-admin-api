package school.hei.haapi.service.befiana;

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
  private static final String BASE_PATH = "/api/sms/v2";
  private static final String MESSAGES_PATH = "/messages/";
  private static final String MESSAGE_BATCHES_PATH = "/message-batches/";

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String apiKey;

  public BefianaClient(RestTemplate restTemplate, String baseUrl, String apiKey) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  public BefianaMessageResponse sendSingle(String to, String text, String idempotencyKey) {
    var body = Map.of("to", to, "text", text);
    return exchange(
        BASE_PATH + MESSAGES_PATH,
        HttpMethod.POST,
        body,
        BefianaMessageResponse.class,
        idempotencyKey);
  }

  public BefianaBatchResponse createBatch(
      List<String> recipients, String text, Instant sendAt, String idempotencyKey) {
    var body =
        Map.of(
            "recipients",
            recipients,
            "text",
            text,
            "send_at",
            DateTimeFormatter.ISO_INSTANT.format(sendAt));
    return exchange(
        BASE_PATH + MESSAGE_BATCHES_PATH,
        HttpMethod.POST,
        body,
        BefianaBatchResponse.class,
        idempotencyKey);
  }

  public BefianaMessageResponse getMessage(String messageId) {
    return exchange(
        BASE_PATH + "/messages/" + messageId + "/",
        HttpMethod.GET,
        null,
        BefianaMessageResponse.class,
        null);
  }

  public BefianaBatchResponse getBatch(String batchId) {
    return exchange(
        BASE_PATH + "/message-batches/" + batchId + "/",
        HttpMethod.GET,
        null,
        BefianaBatchResponse.class,
        null);
  }

  public BefianaBatchMessagesPage getBatchMessages(String batchId, String cursor) {
    var uriBuilder =
        UriComponentsBuilder.fromHttpUrl(
            baseUrl + BASE_PATH + "/message-batches/" + batchId + "/messages/");
    if (cursor != null) {
      uriBuilder.queryParam("cursor", cursor);
    }
    try {
      var response =
          restTemplate.exchange(
              uriBuilder.toUriString(),
              HttpMethod.GET,
              new HttpEntity<>(headers(null)),
              BefianaBatchMessagesPage.class);
      return response.getBody();
    } catch (RestClientException e) {
      throw toDetailedBefianaException(
          "BEFIANA get-batch-messages call failed for batch " + batchId, e);
    }
  }

  public BefianaBalanceResponse getBalance() {
    return exchange(
        BASE_PATH + "/balance/", HttpMethod.GET, null, BefianaBalanceResponse.class, null);
  }

  private <T> T exchange(
      String path, HttpMethod method, Object body, Class<T> responseType, String idempotencyKey) {
    try {
      var response =
          restTemplate.exchange(
              baseUrl + path,
              method,
              new HttpEntity<>(body, headers(idempotencyKey)),
              responseType);
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

  private HttpHeaders headers(String idempotencyKey) {
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKey);
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    if (idempotencyKey != null) {
      headers.set("Idempotency-Key", idempotencyKey);
    }
    return headers;
  }
}
