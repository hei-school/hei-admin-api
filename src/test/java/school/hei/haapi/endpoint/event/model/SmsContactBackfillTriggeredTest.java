package school.hei.haapi.endpoint.event.model;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SmsContactBackfillTriggeredTest {
  ObjectMapper om = new ObjectMapper();

  @Test
  void sms_contact_backfill_triggered() throws JsonProcessingException {
    var event = new SmsContactBackfillTriggered();

    var s = om.writeValueAsString(event);
    var dese = om.readValue(s, SmsContactBackfillTriggered.class);

    assertEquals(event, dese);
    assertEquals(ofMinutes(10), event.maxConsumerDuration());
    assertEquals(ofMinutes(1), event.maxConsumerBackoffBetweenRetries());
  }
}
