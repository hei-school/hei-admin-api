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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  void send_without_sendAt_calls_the_immediate_endpoint_with_the_api_key_header() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaSendResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaSendResponse()));

    subject.send("321111111", "Hello", null);

    var entity = captureRequestEntity("/api/smsko/v1/send/");
    assertEquals("my-api-key", entity.getHeaders().getFirst("Authorization"));
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("321111111", body.get("phone_number"));
    assertEquals("Hello", body.get("message"));
    assertTrue(!body.containsKey("send_at"));
  }

  @Test
  void send_with_sendAt_calls_the_scheduled_endpoint_with_a_formatted_date() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(BefianaSendResponse.class)))
        .thenReturn(ResponseEntity.ok(new BefianaSendResponse()));

    subject.send("321111111", "Hello", Instant.parse("2026-07-06T20:05:00Z"));

    var entity = captureRequestEntity("/api/smsko/v1/sendlater/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("2026-07-06 23:05", body.get("send_at"));
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

    subject.sendBulk(List.of("321111111", "321111112"), "Hello all", null);

    var entity = captureRequestEntity("/api/smsko/v1/sendbulk/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals(List.of("321111111", "321111112"), body.get("phone_numbers"));
    assertEquals("Hello all", body.get("message"));
  }

  @Test
  void sendBulk_with_sendAt_formats_the_given_date_instead_of_now() {
    when(restTemplateMock.exchange(
            anyString(),
            any(),
            any(),
            eq(school.hei.haapi.service.befiana.BefianaSendBulkResponse.class)))
        .thenReturn(
            ResponseEntity.ok(new school.hei.haapi.service.befiana.BefianaSendBulkResponse()));

    subject.sendBulk(List.of("321111111"), "Hello all", Instant.parse("2026-07-06T20:05:00Z"));

    var entity = captureRequestEntity("/api/smsko/v1/sendbulk/");
    var body = (Map<String, Object>) entity.getBody();
    assertEquals("2026-07-06 23:05", body.get("send_at"));
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

    assertThrows(BefianaException.class, () -> subject.send("321111111", "Hi", null));
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
