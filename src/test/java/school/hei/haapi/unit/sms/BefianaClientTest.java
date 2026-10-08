package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
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
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.befiana.BefianaSendResponse;

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
  void send_calls_the_immediate_endpoint_with_the_api_key_header() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaSendResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaSendResponse()));

    subject.send("321111111", "Hello");

    var entity = captureRequestEntity("/api/smsko/v1/send/");
    assertEquals("my-api-key", entity.getHeaders().getFirst("Authorization"));
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("321111111", body.get("phone_number"));
    assertEquals("Hello", body.get("message"));
    assertTrue(!body.containsKey("send_at"));
  }

  @Test
  void sendBulk_posts_the_whole_number_list_and_the_shared_message() {
    when(restTemplateMock.exchange(
            anyString(),
            any(),
            any(),
            eq(school.hei.haapi.service.befiana.BefianaSendBulkResponse.class)))
        .thenReturn(
            ResponseEntity.ok(new school.hei.haapi.service.befiana.BefianaSendBulkResponse()));

    subject.sendBulk(List.of("321111111", "321111112"), "Hello all");

    var entity = captureRequestEntity("/api/smsko/v1/sendbulk/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals(List.of("321111111", "321111112"), body.get("phone_numbers"));
    assertEquals("Hello all", body.get("message"));
    assertTrue(body.containsKey("send_at"));
  }

  @Test
  void getBalance_returns_zero_and_never_throws_when_the_response_matches_no_known_field() {
    var response = new BefianaBalanceResponse(); // availableBalance left null on purpose
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBalanceResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    assertEquals(0, subject.getBalance());
  }

  @Test
  void getBalance_reads_the_confirmed_field_when_present() {
    var response = new BefianaBalanceResponse();
    response.setAvailableBalance(320);
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaBalanceResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    assertEquals(320, subject.getBalance());
  }

  @Test
  void a_rest_client_error_is_wrapped_as_a_befiana_exception() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaSendResponse.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

    assertThrows(BefianaException.class, () -> subject.send("321111111", "Hi"));
  }

  @Test
  void a_rest_client_error_s_message_includes_the_http_status_and_befiana_s_response_body() {
    var httpError =
        HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            new org.springframework.http.HttpHeaders(),
            "{\"message\":\"Solde insuffisant\"}".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaSendResponse.class)))
        .thenThrow(httpError);

    var exception = assertThrows(BefianaException.class, () -> subject.send("321111111", "Hi"));

    assertEquals(400, exception.getHttpStatus());
    assertTrue(exception.getMessage().contains("400"));
    assertTrue(exception.getMessage().contains("Solde insuffisant"));
  }

  @Test
  void getDeliveryStatus_sends_the_callback_data_as_a_query_param() {
    when(restTemplateMock.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(school.hei.haapi.service.befiana.BefianaDeliveryStatusResponse.class)))
        .thenReturn(
            ResponseEntity.ok(
                new school.hei.haapi.service.befiana.BefianaDeliveryStatusResponse()));

    subject.getDeliveryStatus("befiana-20241011-cd-adc1d60");

    verify(restTemplateMock)
        .exchange(
            eq(
                "https://api.befiana.cloud/api/smsko/v1/get-delivery-status/?callback_data=befiana-20241011-cd-adc1d60"),
            eq(HttpMethod.GET),
            any(),
            eq(school.hei.haapi.service.befiana.BefianaDeliveryStatusResponse.class));
  }

  @Test
  void getDeliveryStatus_wraps_a_rest_client_error_as_a_befiana_exception() {
    when(restTemplateMock.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(school.hei.haapi.service.befiana.BefianaDeliveryStatusResponse.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

    assertThrows(
        BefianaException.class, () -> subject.getDeliveryStatus("befiana-20241011-cd-adc1d60"));
  }
}
