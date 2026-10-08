package school.hei.haapi.endpoint.event.model;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SmsCampaignDispatchRequestedTest {
  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void sms_campaign_dispatch_requested() throws JsonProcessingException {
    var event = new SmsCampaignDispatchRequested("campaign1");

    var serializedJson = objectMapper.writeValueAsString(event);
    var deserializedEvent =
        objectMapper.readValue(serializedJson, SmsCampaignDispatchRequested.class);

    assertEquals(event, deserializedEvent);
    assertEquals("campaign1", event.getCampaignId());
    assertEquals(ofMinutes(5), event.maxConsumerDuration());
    assertEquals(ofMinutes(1), event.maxConsumerBackoffBetweenRetries());
  }
}
