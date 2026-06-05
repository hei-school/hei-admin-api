package school.hei.haapi.endpoint.event.model;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class StudentResultOverviewUpsertedTest {
  ObjectMapper om = new ObjectMapper();

  @Test
  void student_result_overview_upserted() throws JsonProcessingException {
    var event = new StudentResultOverviewUpserted("dummy");

    var s = om.writeValueAsString(event);
    var dese = om.readValue(s, StudentResultOverviewUpserted.class);

    assertEquals(event, dese);
    assertEquals(ofMinutes(10), event.maxConsumerDuration());
    assertEquals(ofSeconds(60), event.maxConsumerBackoffBetweenRetries());
  }
}
