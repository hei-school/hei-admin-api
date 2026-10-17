package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.service.befiana.BefianaBalanceResponse;
import school.hei.haapi.service.befiana.BefianaBatchMessagesPage;
import school.hei.haapi.service.befiana.BefianaBatchResponse;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.befiana.BefianaMessageResponse;

class BefianaClientTest {
  private final RestTemplate restTemplateMock = mock();
  private final BefianaClient subject =
      new BefianaClient(restTemplateMock, "https://api.befiana.cloud", "my-api-key");

  @SuppressWarnings("unchecked")
  private HttpEntity<Object> captureRequestEntity(String expectedPath) {
    var captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock)
        .exchange(
            eq("https://api.befiana.cloud" + expectedPath),
            any(),
            captor.capture(),
            any(Class.class));
    return captor.getValue();
  }

  @Test
  void sendSingle_calls_the_v2_messages_endpoint_with_a_bearer_token_and_idempotency_key() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaMessageResponse()));

    subject.sendSingle("+261340000000", "Hello", "idem-key-1");

    var entity = captureRequestEntity("/api/sms/v2/messages/");
    assertEquals("Bearer my-api-key", entity.getHeaders().getFirst("Authorization"));
    assertEquals("idem-key-1", entity.getHeaders().getFirst("Idempotency-Key"));
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("+261340000000", body.get("to"));
    assertEquals("Hello", body.get("text"));
    assertTrue(!body.containsKey("send_at"));
  }

  @Test
  void createBatch_posts_recipients_text_and_an_iso_send_at() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBatchResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaBatchResponse()));
    var sendAt = Instant.parse("2030-01-15T06:00:00Z");

    subject.createBatch(
        List.of("+261340000000", "+261320000000"), "Hello all", sendAt, "idem-key-2");

    var entity = captureRequestEntity("/api/sms/v2/message-batches/");
    assertEquals("idem-key-2", entity.getHeaders().getFirst("Idempotency-Key"));
    var body = (Map<String, Object>) entity.getBody();
    assertEquals(List.of("+261340000000", "+261320000000"), body.get("recipients"));
    assertEquals("Hello all", body.get("text"));
    assertEquals("2030-01-15T06:00:00Z", body.get("send_at"));
  }

  @Test
  void getMessage_calls_the_single_message_endpoint() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaMessageResponse()));

    subject.getMessage("msg-1");

    verify(restTemplateMock)
        .exchange(
            eq("https://api.befiana.cloud/api/sms/v2/messages/msg-1/"),
            eq(HttpMethod.GET),
            any(),
            eq(BefianaMessageResponse.class));
  }

  @Test
  void getBatch_calls_the_batch_endpoint() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBatchResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaBatchResponse()));

    subject.getBatch("batch-1");

    verify(restTemplateMock)
        .exchange(
            eq("https://api.befiana.cloud/api/sms/v2/message-batches/batch-1/"),
            eq(HttpMethod.GET),
            any(),
            eq(BefianaBatchResponse.class));
  }

  @Test
  void getBatchMessages_omits_the_cursor_param_when_null_and_includes_it_otherwise() {
    when(restTemplateMock.exchange(
            anyString(), eq(HttpMethod.GET), any(), eq(BefianaBatchMessagesPage.class)))
        .thenReturn(ResponseEntity.ok(new BefianaBatchMessagesPage()));

    subject.getBatchMessages("batch-1", null);
    verify(restTemplateMock)
        .exchange(
            eq("https://api.befiana.cloud/api/sms/v2/message-batches/batch-1/messages/"),
            eq(HttpMethod.GET),
            any(),
            eq(BefianaBatchMessagesPage.class));

    subject.getBatchMessages("batch-1", "next-page-token");
    verify(restTemplateMock)
        .exchange(
            eq(
                "https://api.befiana.cloud/api/sms/v2/message-batches/batch-1/messages/?cursor=next-page-token"),
            eq(HttpMethod.GET),
            any(),
            eq(BefianaBatchMessagesPage.class));
  }

  @Test
  void getBalance_returns_the_v2_response_as_is() {
    var response = new BefianaBalanceResponse();
    response.setAvailable(320);
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBalanceResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    assertEquals(320, subject.getBalance().getAvailable());
  }

  @Test
  void sendSingle_converts_a_local_number_to_e164_before_sending() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaMessageResponse()));

    subject.sendSingle("321234567", "Hello", "idem-key-local");

    var entity = captureRequestEntity("/api/sms/v2/messages/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("+261321234567", body.get("to"));
  }

  @Test
  void createBatch_converts_local_numbers_to_e164_and_leaves_e164_numbers_untouched() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBatchResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaBatchResponse()));
    var sendAt = Instant.parse("2030-01-15T06:00:00Z");

    subject.createBatch(
        List.of("321234567", "+261320000000"), "Hello all", sendAt, "idem-key-mixed");

    var entity = captureRequestEntity("/api/sms/v2/message-batches/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals(List.of("+261321234567", "+261320000000"), body.get("recipients"));
  }

  @Test
  void getBalance_can_return_a_null_available_when_befiana_omits_it() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBalanceResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaBalanceResponse()));

    assertNull(subject.getBalance().getAvailable());
  }

  @Test
  void a_rest_client_error_is_wrapped_as_a_befiana_exception() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaMessageResponse.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

    assertThrows(BefianaException.class, () -> subject.sendSingle("+261340000000", "Hi", "k"));
  }

  @Test
  void a_rest_client_error_s_message_includes_the_http_status_and_befiana_s_response_body() {
    var httpError =
        HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            new org.springframework.http.HttpHeaders(),
            "{\"error\":{\"code\":\"insufficient_balance\",\"message\":\"Solde insuffisant\"}}"
                .getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaMessageResponse.class)))
        .thenThrow(httpError);

    var exception =
        assertThrows(BefianaException.class, () -> subject.sendSingle("+261340000000", "Hi", "k"));

    assertEquals(400, exception.getHttpStatus());
    assertTrue(exception.getMessage().contains("400"));
    assertTrue(exception.getMessage().contains("Solde insuffisant"));
  }

  @Test
  void getBatchMessages_wraps_a_rest_client_error_as_a_befiana_exception() {
    when(restTemplateMock.exchange(
            anyString(), eq(HttpMethod.GET), any(), eq(BefianaBatchMessagesPage.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

    assertThrows(BefianaException.class, () -> subject.getBatchMessages("batch-1", null));
  }
}
