package school.hei.haapi.endpoint.event.model;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SmsDeliveryStatusPollTriggeredTest {
  ObjectMapper om = new ObjectMapper();

  @Test
  void sms_delivery_status_poll_triggered() throws JsonProcessingException {
    var event = new SmsDeliveryStatusPollTriggered();

    var s = om.writeValueAsString(event);
    var dese = om.readValue(s, SmsDeliveryStatusPollTriggered.class);

    assertEquals(event, dese);
    assertEquals(ofMinutes(10), event.maxConsumerDuration());
    assertEquals(ofMinutes(1), event.maxConsumerBackoffBetweenRetries());
  }
}
